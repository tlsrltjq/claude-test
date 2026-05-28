package com.eactive.resourcehub.document.dto;

import com.eactive.resourcehub.document.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotNull(message = "문서 종류를 선택해주세요.")
    private DocumentType documentType;

    @NotBlank(message = "문서 제목을 입력해주세요.")
    private String title;

    @NotNull(message = "파일을 첨부해주세요.")
    private MultipartFile file;

    private MultipartFile previewPdf;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiresAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issuedDate;

    private String degreeType;
    private String certTypeMeta;
}
