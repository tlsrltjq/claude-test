package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedFolderService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<Document> findFolderDocuments(Long folderId) {
        return documentRepository.findByFolderIdAndStatusWithVersion(folderId, DocumentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Folder findPublicFolder() {
        return folderRepository.findFirstByType(FolderType.SHARED_PUBLIC)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공용 폴더가 없습니다."));
    }
}
