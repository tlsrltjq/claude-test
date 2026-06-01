package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import com.eactive.resourcehub.user.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.eactive.resourcehub.common.util.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailService {

    private static final int  THUMBNAIL_WIDTH         = 240;
    private static final long MAX_THUMBNAIL_FILE_BYTES = 30L * 1024 * 1024; // 30MB 초과 시 건너뜀
    private static final Set<String> IMAGE_EXTS  = Set.of("jpg", "jpeg", "png");
    // hwp 제외 — LibreOffice 변환 불안정, 미리보기 미지원
    private static final Set<String> OFFICE_EXTS = Set.of("docx", "hwpx", "pptx", "ppt", "xlsx", "xls");

    private final FileStorage fileStorage;
    private final DocumentVersionRepository documentVersionRepository;
    private final AuditService auditService;

    /**
     * 업로드 직후 썸네일 생성 — 실패해도 업로드는 성공으로 유지.
     * REQUIRES_NEW 대신 호출자가 try/catch 로 감싸서 사용.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateAndSave(DocumentVersion version) {
        try {
            byte[] thumbBytes = generate(version);
            if (thumbBytes == null) return;

            String subPath = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String thumbName = UUID.randomUUID() + ".png";
            String thumbPath = fileStorage.store(
                    new ByteArrayInputStreamResource(thumbBytes, "thumbnail.png", thumbBytes.length),
                    subPath, thumbName);

            version.setThumbnail(thumbName, thumbPath, "image/png");
            documentVersionRepository.save(version);
            log.info("썸네일 생성 완료: versionId={}", version.getId());
        } catch (Exception e) {
            log.warn("썸네일 생성 실패 (versionId={}): {}", version.getId(), e.getMessage());
        }
    }

    @Transactional
    public void regenerate(Long documentVersionId, CustomUserDetails userDetails,
                           HttpServletRequest request) {
        DocumentVersion version = documentVersionRepository
                .findByIdWithDocumentAndFolder(documentVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long ownerId = version.getDocument().getFolder().getOwner().getId();
        UserRole role = userDetails.getUser().getRole();
        Long actorId = userDetails.getUser().getId();

        if (role != UserRole.ADMIN && !ownerId.equals(actorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "썸네일 재생성 권한이 없습니다.");
        }

        // Delete old thumbnail if exists
        if (version.getThumbnailStoragePath() != null) {
            try {
                fileStorage.delete(version.getThumbnailStoragePath());
            } catch (IOException e) {
                log.warn("기존 썸네일 삭제 실패: {}", e.getMessage());
            }
        }
        version.clearThumbnail();

        generateAndSave(version);

        auditService.log(actorId, AuditActionType.REGENERATE_THUMBNAIL,
                AuditTargetType.DOCUMENT_VERSION, documentVersionId,
                "썸네일 재생성: versionId=" + documentVersionId, request);
    }

    /**
     * Returns thumbnail bytes, or null if unsupported / preview unavailable.
     * 30MB 초과 파일은 OOM 방지를 위해 건너뜀.
     */
    private byte[] generate(DocumentVersion version) throws Exception {
        String ext = FileUtils.extension(version.getOriginalFileName());

        Long fileSize = version.getFileSize();
        if (fileSize != null && fileSize > MAX_THUMBNAIL_FILE_BYTES) {
            log.info("썸네일 건너뜀 (파일 크기 초과 {}MB): versionId={}", fileSize / 1024 / 1024, version.getId());
            return null;
        }

        if ("pdf".equals(ext)) {
            return pdfFirstPagePng(fileStorage.load(version.getStoragePath()));
        }
        if (IMAGE_EXTS.contains(ext)) {
            return imageThumbnail(fileStorage.load(version.getStoragePath()));
        }
        if (OFFICE_EXTS.contains(ext) && version.getPreviewStoragePath() != null) {
            return pdfFirstPagePng(fileStorage.load(version.getPreviewStoragePath()));
        }
        return null;
    }

    /** PDF 첫 페이지를 PNG로 렌더링. 임시 파일을 통해 PDFBox가 랜덤 접근하도록 하여 메모리 사용 절감. */
    private byte[] pdfFirstPagePng(InputStream in) throws IOException {
        Path tempFile = Files.createTempFile("resourcehub_thumb_", ".pdf");
        try {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            try (PDDocument pdf = Loader.loadPDF(tempFile.toFile())) {
                PDFRenderer renderer = new PDFRenderer(pdf);
                BufferedImage img = renderer.renderImageWithDPI(0, 96, ImageType.RGB);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Thumbnails.of(img).width(THUMBNAIL_WIDTH).outputFormat("png").toOutputStream(out);
                return out.toByteArray();
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private byte[] imageThumbnail(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(in).width(THUMBNAIL_WIDTH).outputFormat("png").toOutputStream(out);
        in.close();
        return out.toByteArray();
    }

    /**
     * Adapter so we can use FileStorage.store(MultipartFile-like, ...) with raw bytes.
     */
    private static class ByteArrayInputStreamResource
            implements org.springframework.web.multipart.MultipartFile {
        private final byte[] data;
        private final String name;
        private final long size;

        ByteArrayInputStreamResource(byte[] data, String name, long size) {
            this.data = data;
            this.name = name;
            this.size = size;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return "image/png"; }
        @Override public boolean isEmpty() { return data.length == 0; }
        @Override public long getSize() { return size; }
        @Override public byte[] getBytes() { return data; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
        @Override public void transferTo(java.io.File dest) throws IOException {
            try (var out = new java.io.FileOutputStream(dest)) { out.write(data); }
        }
    }
}
