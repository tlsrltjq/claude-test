package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.util.FileUtils;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DocumentPreviewResolver {

    private static final Set<String> IMAGE_EXTS  = Set.of("jpg", "jpeg", "png");
    // hwp: LibreOffice 변환 품질 불안정 — 미리보기 미지원
    private static final Set<String> OFFICE_EXTS = Set.of("docx", "hwpx", "pptx", "ppt", "xlsx", "xls");

    public String resolve(DocumentVersion version) {
        if (version == null) return "none";
        String ext = FileUtils.extension(version.getOriginalFileName());
        if ("pdf".equals(ext)) return "pdf";
        if (IMAGE_EXTS.contains(ext)) return "image";
        if (OFFICE_EXTS.contains(ext))
            return version.getPreviewStoragePath() != null ? "pdf" : "none";
        return "none";
    }
}
