package com.eactive.resourcehub.common;

import com.eactive.resourcehub.common.file.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 시드 데이터(demo/*)의 가짜 스토리지 경로를 실제 샘플 PDF로 교체한다.
 * 앱 기동 시 1회 실행. demo/* 경로가 없으면 즉시 종료(멱등).
 */
@Slf4j
@Profile("!prod")
@Component
@RequiredArgsConstructor
public class SampleDataFixRunner {

    private static final String SAMPLE_DIR  = "sample";
    private static final String SAMPLE_FILE = "placeholder.pdf";
    static final String SAMPLE_PATH = SAMPLE_DIR + "/" + SAMPLE_FILE;

    private final FileStorage fileStorage;
    private final JdbcTemplate jdbc;

    @EventListener(ApplicationReadyEvent.class)
    public void fixDemoPaths() {
        try {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM document_versions WHERE storage_path LIKE 'demo/%'",
                    Integer.class);
            if (count == null || count == 0) {
                return; // 대상 없음
            }

            uploadSamplePdf();
            applyToDb();
            log.info("SampleDataFix 완료 — {}개 document_version 경로 교체", count);
        } catch (Exception e) {
            log.warn("SampleDataFix 실패 (앱 기동은 계속됨): {}", e.getMessage());
        }
    }

    private void uploadSamplePdf() throws IOException {
        byte[] pdf = buildPdf();
        fileStorage.store(new BytesFile(pdf, SAMPLE_FILE, "application/pdf"), SAMPLE_DIR, SAMPLE_FILE);
        log.info("샘플 PDF 업로드: {}", SAMPLE_PATH);
    }

    private void applyToDb() {
        // storage_path: 직접 파일 → 샘플 PDF
        jdbc.update(
                "UPDATE document_versions SET storage_path = ? WHERE storage_path LIKE 'demo/%'",
                SAMPLE_PATH);

        // preview_storage_path: 오피스 문서 미리보기용 → 샘플 PDF
        jdbc.update(
                "UPDATE document_versions SET preview_storage_path = ? " +
                "WHERE preview_storage_path LIKE 'demo/%'",
                SAMPLE_PATH);

        // thumbnail_storage_path: NULL로 비워 기본 아이콘으로 폴백
        jdbc.update(
                "UPDATE document_versions SET thumbnail_storage_path = NULL, " +
                "thumbnail_file_name = NULL, thumbnail_content_type = NULL, thumbnail_generated_at = NULL " +
                "WHERE thumbnail_storage_path LIKE 'demo/%'");
    }

    private byte[] buildPdf() throws IOException {
        PDType1Font bold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // 배경
                cs.setNonStrokingColor(0.96f, 0.98f, 1.0f);
                cs.addRect(0, 0, 595, 842);
                cs.fill();

                // 상단 바
                cs.setNonStrokingColor(0.11f, 0.31f, 0.60f);
                cs.addRect(0, 792, 595, 50);
                cs.fill();

                // 제목 (흰색)
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.beginText();
                cs.setFont(bold, 18);
                cs.newLineAtOffset(40, 808);
                cs.showText("eActive Resource Hub");
                cs.endText();

                // 구분선
                cs.setStrokingColor(0.75f, 0.85f, 0.95f);
                cs.setLineWidth(1f);
                cs.moveTo(40, 760);
                cs.lineTo(555, 760);
                cs.stroke();

                // 본문 제목
                cs.setNonStrokingColor(0.11f, 0.31f, 0.60f);
                cs.beginText();
                cs.setFont(bold, 22);
                cs.newLineAtOffset(40, 720);
                cs.showText("Sample Document");
                cs.endText();

                // 설명
                cs.setNonStrokingColor(0.35f, 0.35f, 0.35f);
                cs.beginText();
                cs.setFont(normal, 13);
                cs.setLeading(22f);
                cs.newLineAtOffset(40, 680);
                cs.showText("This is a placeholder file for demo/seed data.");
                cs.newLine();
                cs.showText("The original file does not exist in this environment.");
                cs.newLine();
                cs.newLine();
                cs.setFont(normal, 11);
                cs.setNonStrokingColor(0.55f, 0.55f, 0.55f);
                cs.showText("All demo document versions have been mapped to this file.");
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    /** FileStorage.store() 용 경량 MultipartFile 어댑터 */
    private record BytesFile(byte[] data, String name, String contentType)
            implements MultipartFile {
        @Override public String getName()             { return name; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType()      { return contentType; }
        @Override public boolean isEmpty()            { return data.length == 0; }
        @Override public long getSize()               { return data.length; }
        @Override public byte[] getBytes()            { return data; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
        @Override public void transferTo(java.io.File dest) throws IOException {
            try (var out = new java.io.FileOutputStream(dest)) { out.write(data); }
        }
    }
}
