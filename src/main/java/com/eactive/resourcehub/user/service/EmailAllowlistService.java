package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.util.FileMagicValidator;
import com.eactive.resourcehub.common.util.FileUtils;
import com.eactive.resourcehub.user.entity.AllowedEmail;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.repository.AllowedEmailRepository;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailAllowlistService {

    private final AllowedEmailRepository allowedEmailRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean isAllowed(String email) {
        return allowedEmailRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<AllowedEmail> findAll() {
        return allowedEmailRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public AllowedEmail add(String email, String note, UserRole initialRole, Long adminUserId) {
        String normalized = email.trim().toLowerCase();
        if (allowedEmailRepository.existsByEmail(normalized)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + normalized);
        }
        User admin = userRepository.findById(adminUserId).orElseThrow();
        return allowedEmailRepository.save(AllowedEmail.create(normalized, note, initialRole, admin));
    }

    /** 텍스트 일괄 등록: 쉼표·공백·줄바꿈 구분 */
    @Transactional
    public BulkResult addBulk(String rawText, UserRole initialRole, Long adminUserId) {
        User admin = userRepository.findById(adminUserId).orElseThrow();
        List<String> emails = splitEmails(rawText);
        return saveBulk(emails, initialRole, admin);
    }

    private static final int MAX_EXCEL_ROWS = 1_000;

    /** 엑셀 일괄 등록: 첫 번째 열의 이메일 추출 */
    @Transactional
    public BulkResult addBulkFromExcel(MultipartFile file, UserRole initialRole, Long adminUserId) throws IOException {
        String ext = FileUtils.extension(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (!ext.equals("xlsx") && !ext.equals("xls")) {
            throw new IllegalArgumentException("xlsx 또는 xls 파일만 업로드할 수 있습니다.");
        }
        if (!FileMagicValidator.validate(file, ext)) {
            throw new IllegalArgumentException("파일 형식이 올바르지 않습니다.");
        }

        User admin = userRepository.findById(adminUserId).orElseThrow();
        List<String> emails = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int rowCount = 0;
            for (Row row : sheet) {
                if (++rowCount > MAX_EXCEL_ROWS) {
                    throw new IllegalArgumentException("엑셀 파일은 최대 " + MAX_EXCEL_ROWS + "행까지 처리할 수 있습니다.");
                }
                Cell cell = row.getCell(0);
                if (cell == null) continue;
                String val = cell.getCellType() == CellType.STRING
                        ? cell.getStringCellValue().trim()
                        : cell.toString().trim();
                if (!val.isBlank()) emails.add(val);
            }
        }
        return saveBulk(emails, initialRole, admin);
    }

    private BulkResult saveBulk(List<String> emails, UserRole initialRole, User admin) {
        int added = 0, skipped = 0;
        for (String raw : emails) {
            String email = raw.trim().toLowerCase();
            if (!email.contains("@")) { skipped++; continue; }
            if (allowedEmailRepository.existsByEmail(email)) { skipped++; continue; }
            allowedEmailRepository.save(AllowedEmail.create(email, null, initialRole, admin));
            added++;
        }
        return new BulkResult(added, skipped);
    }

    private List<String> splitEmails(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        for (String token : raw.split("[,\\s]+")) {
            String t = token.trim();
            if (!t.isBlank()) result.add(t);
        }
        return result;
    }

    @Transactional
    public void remove(Long id) {
        if (!allowedEmailRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 허용 이메일입니다.");
        }
        allowedEmailRepository.deleteById(id);
    }

    public record BulkResult(int added, int skipped) {}
}
