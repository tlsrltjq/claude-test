package com.eactive.resourcehub.document.dto;

import com.eactive.resourcehub.document.entity.DocumentType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentUploadRequest {

    private DocumentType documentType;
    private String title;
    private MultipartFile file;
    private MultipartFile previewPdf;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedDate;

    private String degreeType;
    private String certTypeMeta;
}
