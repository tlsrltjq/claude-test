package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.audit.service.AuditLogService;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
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
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static final long LARGE_FILE_THRESHOLD = 10L * 1024 * 1024; // 10 MB

    private final FileStorage fileStorage;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AuditLogService auditLogService;
    private final ThumbnailService thumbnailService;

    @Value("${resourcehub.upload.allowed-extensions:pdf,jpg,jpeg,png,docx,hwp,hwpx}")
    private String allowedExtensionsRaw;

    @Transactional
    public Document upload(Long ownerId, DocumentUploadRequest req, HttpServletRequest httpRequest) {
        Folder folder = folderRepository.findByOwnerIdAndType(ownerId, FolderType.PERSONAL)
                .orElseThrow(() -> new IllegalStateException("개인 폴더가 없습니다."));

        validateFile(req.getFile());

        String checksum = checksum(req.getFile());
        if (checksum != null) {
            documentVersionRepository.findFirstByChecksumInFolder(checksum, folder.getId(), DocumentStatus.DELETED)
                    .ifPresent(dup -> { throw new IllegalArgumentException(
                            "동일한 파일이 이미 '" + dup.getDocument().getTitle() + "'에 업로드되어 있습니다."); });
        }

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
            try {
                document = Document.create(folder, req.getDocumentType(), req.getTitle());
                documentRepository.saveAndFlush(document);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("동일한 문서가 이미 업로드 중입니다. 잠시 후 다시 시도하세요.");
            }
            versionNo = 1;
            isNew = true;
        }

        if (req.getExpiresAt() != null)  document.updateExpiresAt(req.getExpiresAt());
        if (req.getIssuedDate() != null)  document.updateIssuedDate(req.getIssuedDate());
        if (req.getDegreeType() != null && !req.getDegreeType().isBlank())
            document.updateDegreeType(req.getDegreeType());
        if (req.getCertTypeMeta() != null && !req.getCertTypeMeta().isBlank())
            document.updateCertTypeMeta(req.getCertTypeMeta());

        String subPath = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String storedFileName = UUID.randomUUID() + "." + FileUtils.extension(req.getFile().getOriginalFilename());
        String storagePath = storeFile(req.getFile(), subPath, storedFileName);

        User uploader = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DocumentVersion version = DocumentVersion.create(
                document, versionNo,
                req.getFile().getOriginalFilename(),
                storedFileName,
                storagePath,
                req.getFile().getSize(),
                req.getFile().getContentType(),
                checksum,
                uploader
        );

        try {
            documentVersionRepository.save(version);
        } catch (Exception e) {
            deleteSilently(storagePath);
            throw e;
        }

        boolean requiresApproval = req.getFile().getSize() >= LARGE_FILE_THRESHOLD;

        if (!requiresApproval) {
            version.autoApprove();
            document.setCurrentVersion(version);
        }

        AuditActionType action = isNew ? AuditActionType.UPLOAD : AuditActionType.UPDATE_DOCUMENT;
        auditService.log(ownerId, action, AuditTargetType.DOCUMENT, document.getId(),
                "버전 " + versionNo + " 업로드", httpRequest);

        if (requiresApproval) {
            auditLogService.logSubmitReview(ownerId, version.getId(), httpRequest);
        }

        log.info("문서 업로드 — ownerId={}, docId={}, version={}, requiresApproval={}",
                ownerId, document.getId(), versionNo, requiresApproval);

        try {
            thumbnailService.generateAndSave(version);
        } catch (Exception e) {
            log.warn("썸네일 생성 실패 (업로드는 성공): {}", e.getMessage());
        }

        return document;
    }

    /** 지정 폴더에 업로드 (공용 폴더 등 특정 폴더 대상) — 업로더는 uploaderId */
    @Transactional
    public Document uploadToFolder(Long folderId, Long uploaderId,
                                   DocumentUploadRequest req, HttpServletRequest httpRequest) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalStateException("폴더를 찾을 수 없습니다."));

        validateFile(req.getFile());

        String checksum = checksum(req.getFile());
        if (checksum != null) {
            documentVersionRepository.findFirstByChecksumInFolder(checksum, folderId, DocumentStatus.DELETED)
                    .ifPresent(dup -> { throw new IllegalArgumentException(
                            "동일한 파일이 이미 '" + dup.getDocument().getTitle() + "'에 업로드되어 있습니다."); });
        }

        Document document;
        try {
            document = Document.create(folder, req.getDocumentType(), req.getTitle());
            documentRepository.saveAndFlush(document);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("동일한 문서가 이미 업로드 중입니다. 잠시 후 다시 시도하세요.");
        }

        if (req.getExpiresAt() != null) {
            document.updateExpiresAt(req.getExpiresAt());
        }

        String subPath = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String storedFileName = UUID.randomUUID() + "." + FileUtils.extension(req.getFile().getOriginalFilename());
        String storagePath = storeFile(req.getFile(), subPath, storedFileName);

        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DocumentVersion version = DocumentVersion.create(
                document, 1,
                req.getFile().getOriginalFilename(),
                storedFileName,
                storagePath,
                req.getFile().getSize(),
                req.getFile().getContentType(),
                checksum,
                uploader
        );

        try {
            documentVersionRepository.save(version);
        } catch (Exception e) {
            deleteSilently(storagePath);
            throw e;
        }

        boolean requiresApproval = req.getFile().getSize() >= LARGE_FILE_THRESHOLD;
        if (!requiresApproval) {
            version.autoApprove();
            document.setCurrentVersion(version);
        }

        auditService.log(uploaderId, AuditActionType.UPLOAD, AuditTargetType.DOCUMENT, document.getId(),
                "공용 폴더 업로드", httpRequest);

        if (requiresApproval) {
            auditLogService.logSubmitReview(uploaderId, version.getId(), httpRequest);
        }

        log.info("공용 폴더 업로드 — folderId={}, docId={}, uploaderId={}", folderId, document.getId(), uploaderId);

        try {
            thumbnailService.generateAndSave(version);
        } catch (Exception e) {
            log.warn("썸네일 생성 실패 (업로드는 성공): {}", e.getMessage());
        }

        return document;
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

    private static String checksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = file.getBytes();
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }
}
