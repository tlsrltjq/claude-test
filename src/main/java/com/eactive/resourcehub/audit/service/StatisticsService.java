package com.eactive.resourcehub.audit.service;

import com.eactive.resourcehub.audit.dto.DownloadStatsDto;
import com.eactive.resourcehub.audit.dto.TopDocumentDto;
import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.repository.AuditLogRepository;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final AuditLogRepository auditLogRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Transactional(readOnly = true)
    public DownloadStatsDto getDownloadStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart  = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long total = auditLogRepository.countByActionType(AuditActionType.DOWNLOAD);
        long today = auditLogRepository.countByActionTypeAndCreatedAtAfter(AuditActionType.DOWNLOAD, todayStart);
        long week  = auditLogRepository.countByActionTypeAndCreatedAtAfter(AuditActionType.DOWNLOAD, weekStart);
        long month = auditLogRepository.countByActionTypeAndCreatedAtAfter(AuditActionType.DOWNLOAD, monthStart);

        return new DownloadStatsDto(total, today, week, month);
    }

    @Transactional(readOnly = true)
    public List<TopDocumentDto> getTopDownloadedDocuments(int limit) {
        List<Object[]> rows = auditLogRepository.findTopTargetsByActionType(
                AuditActionType.DOWNLOAD, PageRequest.of(0, limit));

        List<Long> versionIds = rows.stream()
                .map(r -> (Long) r[0])
                .collect(Collectors.toList());

        if (versionIds.isEmpty()) return List.of();

        List<DocumentVersion> versions = documentVersionRepository.findByIdInWithOwnerAndTeam(versionIds);
        Map<Long, DocumentVersion> versionMap = versions.stream()
                .collect(Collectors.toMap(DocumentVersion::getId, v -> v));

        List<TopDocumentDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long versionId = (Long) row[0];
            long count = (Long) row[1];
            DocumentVersion dv = versionMap.get(versionId);
            if (dv == null) continue;
            String ownerName = dv.getDocument().getFolder().getOwner().getName();
            String teamName = dv.getDocument().getFolder().getOwner().getTeam() != null
                    ? dv.getDocument().getFolder().getOwner().getTeam().getName() : "-";
            result.add(new TopDocumentDto(
                    versionId,
                    dv.getDocument().getTitle(),
                    dv.getOriginalFileName(),
                    ownerName,
                    teamName,
                    count
            ));
        }
        return result;
    }
}
