package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.exception.ResourceNotFoundException;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentReviewStatus;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.eactive.resourcehub.common.util.FileUtils;
import com.eactive.resourcehub.common.util.FileMagicValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadService {

    private static final long LARGE_FILE_THRESHOLD = 20L * 1024 * 1024; // 20 MB

    private final FileStorage fileStorage;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ThumbnailService thumbnailService;
    private final OfficePreviewService officePreviewService;

    @Value("${resourcehub.upload.allowed-extensions:pdf,jpg,jpeg,png,docx,hwp,hwpx}")
    private String allowedExtensionsRaw;

    /** 개인 폴더 업로드 — 같은 타입·제목이 있으면 버전 추가 */
    @Transactional
    public Document upload(Long ownerId, DocumentUploadRequest req, HttpServletRequest httpRequest) {
        Folder folder = folderRepository.findByOwnerIdAndType(ownerId, FolderType.PERSONAL)
                .orElseThrow(() -> new ResourceNotFoundException("개인 폴더가 없습니다."));

        validateFile(req.getFile());
        String checksum = computeChecksum(req.getFile());
        checkDuplicate(checksum, folder.getId());

        Optional<Document> existing = documentRepository
                .findByFolderIdAndDocumentTypeAndTitle(folder.getId(), req.getDocumentType(), req.getTitle());

        Document document;
        int versionNo;
        boolean isNew;

        if (existing.isPresent()) {
            document = existing.get();
            versionNo = documentVersionRepository.countByDocumentId(document.getId()) + 1;
            isNew = false;
        } else {
            document = createDocument(folder, req);
            versionNo = 1;
            isNew = true;
        }

        applyDocumentMeta(document, req);

        DocumentVersion version = buildAndSaveVersion(document, versionNo, req.getFile(), checksum, ownerId);
        handleApproval(document, version);
        recordAudit(ownerId, document, version, versionNo, isNew, httpRequest);

        generateThumbnailSilently(version);
        return document;
    }

    /** 지정 폴더 업로드 (공용 폴더 등) — 항상 새 Document 생성 */
    @Transactional
    public Document uploadToFolder(Long folderId, Long uploaderId,
                                   DocumentUploadRequest req, HttpServletRequest httpRequest) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("폴더를 찾을 수 없습니다."));

        validateFile(req.getFile());
        String checksum = computeChecksum(req.getFile());
        checkDuplicate(checksum, folderId);

        Document document = createDocument(folder, req);
        if (req.getExpiresAt() != null) document.updateExpiresAt(req.getExpiresAt());

        DocumentVersion version = buildAndSaveVersion(document, 1, req.getFile(), checksum, uploaderId);
        handleApproval(document, version);

        auditService.log(uploaderId, AuditActionType.UPLOAD, AuditTargetType.DOCUMENT,
                document.getId(), "공용 폴더 업로드", httpRequest);
        if (version.getReviewStatus() == DocumentReviewStatus.PENDING_REVIEW) {
            auditService.log(uploaderId, AuditActionType.SUBMIT_REVIEW,
                    AuditTargetType.DOCUMENT_VERSION, version.getId(), "검토 요청", httpRequest);
        }

        log.info("공용 폴더 업로드 — folderId={}, docId={}, uploaderId={}", folderId, document.getId(), uploaderId);
        generateThumbnailSilently(version);
        return document;
    }

    // ── private helpers ──────────────────────────────────────────

    private Document createDocument(Folder folder, DocumentUploadRequest req) {
        try {
            Document doc = Document.create(folder, req.getDocumentType(), req.getTitle());
            return documentRepository.saveAndFlush(doc);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("동일한 문서가 이미 업로드 중입니다. 잠시 후 다시 시도하세요.");
        }
    }

    private void applyDocumentMeta(Document document, DocumentUploadRequest req) {
        if (req.getExpiresAt() != null)  document.updateExpiresAt(req.getExpiresAt());
        if (req.getIssuedDate() != null) document.updateIssuedDate(req.getIssuedDate());
        if (req.getDegreeType() != null && !req.getDegreeType().isBlank())
            document.updateDegreeType(req.getDegreeType());
        if (req.getCertTypeMeta() != null && !req.getCertTypeMeta().isBlank())
            document.updateCertTypeMeta(req.getCertTypeMeta());
    }

    private DocumentVersion buildAndSaveVersion(Document document, int versionNo,
                                                MultipartFile file, String checksum, Long uploaderId) {
        String subPath      = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String storedName   = UUID.randomUUID() + "." + FileUtils.extension(file.getOriginalFilename());
        String storagePath  = storeFile(file, subPath, storedName);

        boolean committed = false;
        try {
            User uploader = userRepository.findById(uploaderId)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

            DocumentVersion version = DocumentVersion.create(
                    document, versionNo,
                    file.getOriginalFilename(),
                    storedName,
                    storagePath,
                    file.getSize(),
                    file.getContentType(),
                    checksum,
                    uploader);

            documentVersionRepository.save(version);
            committed = true;
            return version;
        } finally {
            if (!committed) deleteSilently(storagePath);
        }
    }

    private void handleApproval(Document document, DocumentVersion version) {
        boolean requiresApproval = version.getFileSize() >= LARGE_FILE_THRESHOLD;
        if (!requiresApproval) {
            version.autoApprove();
            document.setCurrentVersion(version);
        }
    }

    private void recordAudit(Long userId, Document document, DocumentVersion version,
                              int versionNo, boolean isNew, HttpServletRequest request) {
        AuditActionType action = isNew ? AuditActionType.UPLOAD : AuditActionType.UPDATE_DOCUMENT;
        auditService.log(userId, action, AuditTargetType.DOCUMENT,
                document.getId(), "버전 " + versionNo + " 업로드", request);

        boolean requiresApproval = version.getReviewStatus() == DocumentReviewStatus.PENDING_REVIEW;
        if (requiresApproval) {
            auditService.log(userId, AuditActionType.SUBMIT_REVIEW,
                    AuditTargetType.DOCUMENT_VERSION, version.getId(), "검토 요청", request);
        }
        log.info("문서 업로드 — userId={}, docId={}, version={}, requiresApproval={}",
                userId, document.getId(), versionNo, requiresApproval);
    }

    private void generateThumbnailSilently(DocumentVersion version) {
        String ext = FileUtils.extension(version.getOriginalFileName());
        if (officePreviewService.supports(ext)) {
            // 오피스 파일: PDF 변환 후 썸네일 생성까지 OfficePreviewService에서 처리
            officePreviewService.convertAndSave(version);
        } else {
            try {
                thumbnailService.generateAndSave(version);
            } catch (Exception e) {
                log.warn("썸네일 생성 실패 (업로드는 성공): {}", e.getMessage());
            }
        }
    }

    private void checkDuplicate(String checksum, Long folderId) {
        if (checksum == null) return;
        documentVersionRepository
                .findFirstByChecksumInFolder(checksum, folderId, DocumentStatus.DELETED)
                .ifPresent(dup -> { throw new IllegalArgumentException(
                        "동일한 파일이 이미 '" + dup.getDocument().getTitle() + "'에 업로드되어 있습니다."); });
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        String ext = FileUtils.extension(file.getOriginalFilename());
        List<String> allowed = Arrays.asList(allowedExtensionsRaw.split(","));
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + ext);
        }
        try {
            if (!FileMagicValidator.validate(file, ext)) {
                throw new IllegalArgumentException("파일 내용이 확장자(" + ext + ")와 일치하지 않습니다.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("파일을 읽을 수 없습니다.");
        }
    }

    private String storeFile(MultipartFile file, String subPath, String storedName) {
        try {
            return fileStorage.store(file, subPath, storedName);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    private void deleteSilently(String path) {
        try {
            fileStorage.delete(path);
        } catch (IOException ignore) {
        }
    }

    private static String computeChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }
}
