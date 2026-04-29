package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.dto.DocumentUploadRequest;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadService {

    private final FileStorage fileStorage;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ThumbnailService thumbnailService;

    @Value("${resourcehub.upload.allowed-extensions:pdf,jpg,jpeg,png,docx,hwp,hwpx}")
    private String allowedExtensionsRaw;

    @Transactional
    public Document upload(Long ownerId, DocumentUploadRequest req, HttpServletRequest httpRequest) {
        Folder folder = folderRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalStateException("개인 폴더가 없습니다."));

        validateFile(req.getFile(), false);

        MultipartFile previewPdf = req.getPreviewPdf();
        boolean hasPreview = previewPdf != null && !previewPdf.isEmpty();
        if (hasPreview) {
            validatePreviewPdf(previewPdf);
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
            document = Document.create(folder, req.getDocumentType(), req.getTitle());
            documentRepository.saveAndFlush(document);
            versionNo = 1;
            isNew = true;
        }

        String subPath = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        String storedFileName = UUID.randomUUID() + "." + extension(req.getFile().getOriginalFilename());
        String storagePath = storeFile(req.getFile(), subPath, storedFileName);

        User uploader = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String checksum = checksum(req.getFile());

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

        if (hasPreview) {
            String previewStored = UUID.randomUUID() + ".pdf";
            String previewPath;
            try {
                previewPath = fileStorage.store(previewPdf, subPath, previewStored);
            } catch (IOException e) {
                deleteSilently(storagePath);
                throw new RuntimeException("미리보기 파일 저장 실패", e);
            }
            version.setPreview(previewPdf.getOriginalFilename(), previewPath);
        }

        try {
            documentVersionRepository.save(version);
        } catch (Exception e) {
            deleteSilently(storagePath);
            if (version.getPreviewStoragePath() != null) {
                deleteSilently(version.getPreviewStoragePath());
            }
            throw e;
        }

        document.setCurrentVersion(version);

        AuditActionType action = isNew ? AuditActionType.UPLOAD : AuditActionType.UPDATE_DOCUMENT;
        auditService.log(ownerId, action, AuditTargetType.DOCUMENT, document.getId(),
                "버전 " + versionNo + " 업로드", httpRequest);

        log.info("문서 업로드 — ownerId={}, docId={}, version={}", ownerId, document.getId(), versionNo);

        // 썸네일 생성 (실패해도 업로드 성공 유지)
        try {
            thumbnailService.generateAndSave(version);
        } catch (Exception e) {
            log.warn("썸네일 생성 실패 (업로드는 성공): {}", e.getMessage());
        }

        return document;
    }

    private void validateFile(MultipartFile file, boolean requirePdf) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        String ext = extension(file.getOriginalFilename()).toLowerCase();
        List<String> allowed = Arrays.asList(allowedExtensionsRaw.split(","));
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + ext);
        }
    }

    private void validatePreviewPdf(MultipartFile file) {
        if (file.isEmpty()) return;
        String ext = extension(file.getOriginalFilename()).toLowerCase();
        if (!"pdf".equals(ext)) {
            throw new IllegalArgumentException("미리보기 파일은 PDF만 허용됩니다.");
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

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1);
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
