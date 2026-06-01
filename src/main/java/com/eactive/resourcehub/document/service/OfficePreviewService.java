package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficePreviewService {

    private static final Set<String> SUPPORTED = Set.of("docx", "hwpx", "pptx", "ppt", "xlsx", "xls");
    private static final long CONVERT_TIMEOUT_SEC = 60;

    private final FileStorage fileStorage;
    private final DocumentVersionRepository documentVersionRepository;
    private final ThumbnailService thumbnailService;

    @Value("${app.libreoffice.enabled:true}")
    private boolean libreOfficeEnabled;

    public boolean supports(String ext) {
        return SUPPORTED.contains(ext);
    }

    /**
     * 오피스 파일을 PDF로 변환 후 previewStoragePath에 저장.
     * 변환 실패 시 로그만 남기고 조용히 종료 — 업로드 자체는 성공으로 유지.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void convertAndSave(DocumentVersion version) {
        if (!libreOfficeEnabled) {
            log.info("LibreOffice 비활성화 — 미리보기 변환 건너뜀: versionId={}", version.getId());
            thumbnailService.generateAndSave(version);
            return;
        }

        Path tempInput  = null;
        Path tempOutDir = null;
        try {
            // 원본 파일을 임시 파일로 복사
            String originalName = version.getOriginalFileName();
            String ext = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : "";
            tempInput  = Files.createTempFile("rh_preview_", ext);
            tempOutDir = Files.createTempDirectory("rh_preview_out_");

            try (InputStream src = fileStorage.load(version.getStoragePath())) {
                Files.copy(src, tempInput, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // LibreOffice 변환 실행
            ProcessBuilder pb = new ProcessBuilder(
                    "soffice", "--headless", "--norestore",
                    "--convert-to", "pdf",
                    "--outdir", tempOutDir.toString(),
                    tempInput.toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(CONVERT_TIMEOUT_SEC, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                log.warn("LibreOffice 변환 타임아웃 ({}s): versionId={}", CONVERT_TIMEOUT_SEC, version.getId());
                return;
            }
            if (process.exitValue() != 0) {
                log.warn("LibreOffice 변환 실패 (exitCode={}): versionId={}", process.exitValue(), version.getId());
                return;
            }

            // 변환된 PDF 파일 찾기
            Path pdfFile = Files.list(tempOutDir)
                    .filter(p -> p.toString().endsWith(".pdf"))
                    .findFirst()
                    .orElse(null);
            if (pdfFile == null) {
                log.warn("LibreOffice 변환 결과 PDF 없음: versionId={}", version.getId());
                return;
            }

            // 스토리지에 저장
            byte[] pdfBytes = Files.readAllBytes(pdfFile);
            String subPath   = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String pdfName   = UUID.randomUUID() + ".pdf";
            String storedPath = fileStorage.store(
                    new BytesMultipartFile(pdfBytes, pdfName, "application/pdf"),
                    subPath, pdfName);

            version.setPreview(pdfName, storedPath);
            documentVersionRepository.save(version);
            log.info("오피스 미리보기 변환 완료: versionId={}, size={}KB",
                    version.getId(), pdfBytes.length / 1024);

        } catch (Exception e) {
            log.warn("오피스 미리보기 변환 중 오류 (versionId={}): {}", version.getId(), e.getMessage());
        } finally {
            deleteTempSilently(tempInput);
            if (tempOutDir != null) {
                try {
                    try (var s = Files.list(tempOutDir)) {
                        s.forEach(p -> deleteTempSilently(p));
                    }
                    Files.deleteIfExists(tempOutDir);
                } catch (Exception ignore) {}
            }
            // 변환 성공 여부와 관계없이 썸네일 생성 시도 (previewStoragePath 설정된 경우 활용)
            thumbnailService.generateAndSave(version);
        }
    }

    private void deleteTempSilently(Path path) {
        if (path == null) return;
        try { Files.deleteIfExists(path); } catch (Exception ignore) {}
    }

    /** FileStorage.store() 용 경량 MultipartFile 어댑터 */
    private static class BytesMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final byte[] data;
        private final String name;
        private final String contentType;

        BytesMultipartFile(byte[] data, String name, String contentType) {
            this.data = data; this.name = name; this.contentType = contentType;
        }
        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return data.length == 0; }
        @Override public long getSize() { return data.length; }
        @Override public byte[] getBytes() { return data; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
        @Override public void transferTo(java.io.File dest) throws java.io.IOException {
            try (var out = new java.io.FileOutputStream(dest)) { out.write(data); }
        }
    }
}
