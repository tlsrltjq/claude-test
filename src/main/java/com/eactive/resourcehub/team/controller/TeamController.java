package com.eactive.resourcehub.team.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentStatus;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import com.eactive.resourcehub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;

    // /team/members
    @GetMapping("/members")
    public String teamMembers(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User leader = userDetails.getUser();
        if (leader.getRole() != UserRole.TEAM_LEADER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (leader.getTeam() == null) {
            model.addAttribute("members", List.of());
            return "team/members";
        }

        Long teamId = leader.getTeam().getId();
        List<User> members = userRepository.findByStatusWithTeam(UserStatus.ACTIVE).stream()
                .filter(u -> u.getTeam() != null && u.getTeam().getId().equals(teamId))
                .filter(u -> !u.getId().equals(leader.getId()))
                .toList();

        model.addAttribute("members", members);
        return "team/members";
    }

    // /team/members/{userId}/documents
    @GetMapping("/members/{userId}/documents")
    public String memberDocuments(@PathVariable Long userId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        User leader = userDetails.getUser();
        if (leader.getRole() != UserRole.TEAM_LEADER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (leader.getTeam() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User member = userRepository.findByIdWithTeam(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Long leaderTeamId = leader.getTeam().getId();
        Long memberTeamId = member.getTeam() != null ? member.getTeam().getId() : null;
        if (!leaderTeamId.equals(memberTeamId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "같은 팀 직원만 조회할 수 있습니다.");
        }

        model.addAttribute("member", member);
        folderRepository.findByOwnerId(userId).ifPresent(folder -> {
            List<Document> documents = documentRepository
                    .findByFolderIdAndStatusWithVersion(folder.getId(), DocumentStatus.ACTIVE);
            model.addAttribute("documents", documents);
        });
        return "team/member-documents";
    }
}
