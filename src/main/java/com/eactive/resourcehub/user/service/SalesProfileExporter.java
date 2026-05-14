package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.DocumentType;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SalesProfileExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private record ColDef(String key, String header) {}

    private static final List<ColDef> COL_ORDER = List.of(
        new ColDef("team",                  "팀"),
        new ColDef("position",              "직급"),
        new ColDef(null,                    "이름"),
        new ColDef("age",                   "나이"),
        new ColDef("birthDate",             "생년월일"),
        new ColDef("phone",                 "연락처"),
        new ColDef("email",                 "이메일"),
        new ColDef("developerGrade",        "개발자 등급"),
        new ColDef("career",                "경력"),
        new ColDef("resume",                "이력서"),
        new ColDef("careerDescription",     "경력기술서"),
        new ColDef("license",               "정보처리기사"),
        new ColDef("graduationCertificate", "졸업증명서"),
        new ColDef("healthInsuranceProof",  "건강보험득실"),
        new ColDef("etc",                   "기타자료")
    );

    /** 전체 또는 선택 행 export. visibleCols 비어있으면 전체 컬럼. careerDisplay: ymd/m/d */
    public byte[] export(List<ProfileRow> rows, Set<String> visibleCols, String careerDisplay) {
        boolean showAll = visibleCols == null || visibleCols.isEmpty();
        List<ColDef> effective = COL_ORDER.stream()
                .filter(cd -> cd.key() == null || showAll || visibleCols.contains(cd.key()))
                .toList();
        String cd = careerDisplay != null ? careerDisplay : "ymd";

        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            Sheet sheet = wb.createSheet("인력 표");

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = wb.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle centerStyle = wb.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < effective.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(effective.get(i).header());
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, "email".equals(effective.get(i).key()) ? 7000 : 4500);
            }

            int rowIdx = 1;
            for (ProfileRow row : rows) {
                Row r = sheet.createRow(rowIdx++);
                int col = 0;
                for (ColDef colDef : effective) {
                    String k = colDef.key() != null ? colDef.key() : "name";
                    Cell cell = r.createCell(col++);
                    switch (k) {
                        case "team"     -> cell.setCellValue(row.getUser().getTeam() != null ? row.getUser().getTeam().getName() : "");
                        case "position" -> cell.setCellValue(row.getUser().getPosition() != null ? row.getUser().getPosition().getDisplayName() : "");
                        case "name"     -> cell.setCellValue(row.getUser().getName() != null ? row.getUser().getName() : "");
                        case "age"      -> { if (row.getAge() > 0) cell.setCellValue(row.getAge()); }
                        case "birthDate"-> cell.setCellValue(row.getUser().getBirthDate() != null ? row.getUser().getBirthDate().format(DATE_FMT) : "");
                        case "phone"    -> cell.setCellValue(row.getUser().getPhone() != null ? row.getUser().getPhone() : "");
                        case "email"    -> cell.setCellValue(row.getUser().getEmail() != null ? row.getUser().getEmail() : "");
                        case "developerGrade" -> cell.setCellValue(row.getDeveloperGrade() != null ? row.getDeveloperGrade() : "");
                        case "career"   -> cell.setCellValue(careerText(row, cd));
                        case "resume"               -> docCell(cell, row, DocumentType.RESUME);
                        case "careerDescription"    -> docCell(cell, row, DocumentType.CAREER_DESCRIPTION);
                        case "license"              -> docCell(cell, row, DocumentType.LICENSE);
                        case "graduationCertificate"-> docCell(cell, row, DocumentType.GRADUATION_CERTIFICATE);
                        case "healthInsuranceProof" -> docCell(cell, row, DocumentType.HEALTH_INSURANCE_PROOF);
                        case "etc"                  -> docCell(cell, row, DocumentType.ETC);
                        default -> {}
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.dispose();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("엑셀 생성 실패", e);
        }
    }

    /** 하위호환 — careerDisplay 없이 호출 시 기본값 */
    public byte[] export(List<ProfileRow> rows, Set<String> visibleCols) {
        return export(rows, visibleCols, "ymd");
    }

    private String careerText(ProfileRow row, String careerDisplay) {
        return switch (careerDisplay) {
            case "m" -> row.getCareerMonthsFromDays() > 0 ? row.getCareerMonthsFromDays() + "개월" : "";
            case "d" -> row.getCareerTotalDays() > 0 ? row.getCareerTotalDays() + "일" : "";
            default  -> row.getCareerYmd() != null ? row.getCareerYmd() : "";
        };
    }

    private void docCell(Cell cell, ProfileRow row, DocumentType type) {
        cell.setCellValue(row.getDoc(type) != null ? "O" : "X");
    }
}
