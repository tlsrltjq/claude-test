package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final AuditService auditService;

    @Transactional
    public Folder createPersonalFolder(User owner, Long actorUserId, HttpServletRequest request) {
        if (folderRepository.existsByOwnerIdAndType(owner.getId(), FolderType.PERSONAL)) {
            log.info("개인 폴더 이미 존재 — userId={}", owner.getId());
            return folderRepository.findByOwnerIdAndType(owner.getId(), FolderType.PERSONAL).orElseThrow();
        }
        String folderName = owner.getName() + " 개인 폴더";
        Folder folder = Folder.create(owner, folderName);
        Folder saved = folderRepository.save(folder);
        log.info("개인 폴더 생성 — userId={}, folderId={}", owner.getId(), saved.getId());
        auditService.log(actorUserId, AuditActionType.CREATE,
                AuditTargetType.FOLDER, saved.getId(), "개인 폴더 자동 생성", request);
        return saved;
    }
}
