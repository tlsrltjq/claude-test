package com.eactive.resourcehub.user.service;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesMemberService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    public List<User> findActiveMembers(String q, Long teamId, String sort, String direction) {
        var stream = userRepository.findByStatusWithTeam(UserStatus.ACTIVE).stream()
                .filter(u -> u.getRole() != UserRole.ADMIN)
                .filter(u -> {
                    if (q == null || q.isBlank()) return true;
                    String keyword = q.toLowerCase();
                    String name  = u.getName()  != null ? u.getName().toLowerCase()  : "";
                    String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
                    return name.contains(keyword) || email.contains(keyword);
                })
                .filter(u -> teamId == null || (u.getTeam() != null && teamId.equals(u.getTeam().getId())));

        Comparator<User> cmp = switch (sort != null ? sort : "position") {
            case "name"  -> Comparator.comparing(u -> u.getName() != null ? u.getName() : "");
            case "team"  -> Comparator.comparing(u -> u.getTeam() != null ? u.getTeam().getName() : "");
            case "role"  -> Comparator.comparingInt(u -> u.getRole().ordinal());
            default      -> Comparator.comparingInt(u -> u.getPosition() != null ? u.getPosition().ordinal() : 999);
        };

        if ("desc".equalsIgnoreCase(direction)) cmp = cmp.reversed();

        return stream.sorted(cmp).toList();
    }

    public User findMemberById(Long userId) {
        return userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public List<Document> findMemberDocuments(Long userId) {
        return folderRepository.findByOwnerIdAndType(userId, FolderType.PERSONAL)
                .map(folder -> documentRepository.findByFolderIdAndStatusWithVersion(folder.getId(), DocumentStatus.ACTIVE))
                .orElse(Collections.emptyList());
    }
}
