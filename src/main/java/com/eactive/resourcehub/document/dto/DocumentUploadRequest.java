package com.eactive.resourcehub.document.dto;

import com.eactive.resourcehub.document.entity.DocumentType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DocumentUploadRequest {

    private DocumentType documentType;
    private String title;
    private MultipartFile file;
    private MultipartFile previewPdf;
}
