package com.eactive.resourcehub.user.controller;

import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    @GetMapping("/members")
    public String members(Model model) {
        List<User> members = userRepository.findByStatusWithTeam(UserStatus.ACTIVE);
        model.addAttribute("members", members);
        return "sales/members";
    }

    @GetMapping("/members/{userId}/documents")
    public String memberDocuments(@PathVariable Long userId, Model model) {
        User member = userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        model.addAttribute("member", member);
        folderRepository.findByOwnerId(userId).ifPresent(folder -> {
            List<Document> documents = documentRepository
                    .findByFolderIdAndStatusWithVersion(folder.getId(), DocumentStatus.ACTIVE);
            model.addAttribute("documents", documents);
        });
        return "sales/member-documents";
    }
}
