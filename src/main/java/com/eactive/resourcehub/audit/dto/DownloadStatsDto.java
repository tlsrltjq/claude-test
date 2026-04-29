package com.eactive.resourcehub.audit.dto;

public record DownloadStatsDto(
        long totalCount,
        long todayCount,
        long weekCount,
        long monthCount
) {}
