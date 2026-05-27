package com.eactive.resourcehub.document.controller;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentVersion;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.service.MyFolderService;
import com.eactive.resourcehub.document.service.SharedFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final MyFolderService myFolderService;
    private final SharedFolderService sharedFolderService;

    @GetMapping
    public String workspace(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();

        Folder myFolder = myFolderService.findPersonalFolder(userId).orElse(null);
        if (myFolder != null) {
            List<Document> myDocs = myFolderService.findActiveDocuments(myFolder.getId());
            Map<Long, DocumentVersion> latestVersions = myFolderService.findLatestVersionMap(myDocs);
            model.addAttribute("myFolder", myFolder);
            model.addAttribute("myDocuments", myDocs);
            model.addAttribute("latestVersions", latestVersions);
        }

        Folder publicFolder = sharedFolderService.findPublicFolder();
        List<Document> publicDocs = sharedFolderService.findFolderDocuments(publicFolder.getId());
        model.addAttribute("publicFolder", publicFolder);
        model.addAttribute("publicDocuments", publicDocs);

        model.addAttribute("user", userDetails.getUser());
        return "workspace";
    }
}
