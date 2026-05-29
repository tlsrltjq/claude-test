package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.user.entity.Position;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleDownloadService {

    private final FileStorage fileStorage;

    /** ZIP을 응답 스트림에 직접 씀 — 메모리에 전체 파일을 적재하지 않음. */
    public void buildZip(List<ProfileRow> rows, OutputStream out) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out, java.nio.charset.StandardCharsets.UTF_8)) {
            int fileCount = 0;
            for (ProfileRow row : rows) {
                String dir = buildDirName(row);
                for (DocumentType type : DocumentType.values()) {
                    DocumentVersion dv = row.getDoc(type);
                    if (dv == null) continue;
                    String entryName = dir + "/" + dv.getOriginalFileName();
                    try (InputStream is = fileStorage.load(dv.getStoragePath())) {
                        zos.putNextEntry(new ZipEntry(entryName));
                        is.transferTo(zos);
                        zos.closeEntry();
                        fileCount++;
                    } catch (IOException e) {
                        log.warn("번들 ZIP — 파일 로드 실패, 건너뜀: storagePath={}", dv.getStoragePath());
                    }
                }
            }
            log.info("번들 ZIP 생성 완료 — 직원={}, 파일={}", rows.size(), fileCount);
        }
    }

    private String buildDirName(ProfileRow row) {
        String name = row.getUser().getName() != null ? row.getUser().getName() : "미상";
        Position pos = row.getUser().getPosition();
        String posStr = pos != null ? pos.getDisplayName() : "";
        return posStr.isBlank() ? name : name + "_" + posStr;
    }
}
