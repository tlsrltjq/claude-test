package com.eactive.resourcehub.audit.dto;

public record TopDocumentDto(
        Long versionId,
        String documentTitle,
        String originalFileName,
        String ownerName,
        String teamName,
        long downloadCount
) {}
