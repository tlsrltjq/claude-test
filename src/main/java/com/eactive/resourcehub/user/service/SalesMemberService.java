package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesMemberService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    public List<User> findActiveMembers() {
        return userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
    }

    public User findMemberById(Long userId) {
        return userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public List<Document> findMemberDocuments(Long userId) {
        return folderRepository.findByOwnerId(userId)
                .map(folder -> documentRepository.findByFolderIdAndStatusWithVersion(folder.getId(), DocumentStatus.ACTIVE))
                .orElse(Collections.emptyList());
    }
}
