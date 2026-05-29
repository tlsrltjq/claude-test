package com.eactive.resourcehub.certificate;

import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.service.ProjectService;
import com.eactive.resourcehub.user.service.EmployeeManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final EmployeeManagementService employeeService;
    private final ProjectService projectService;

    @GetMapping
    public String index(Model model) {
        boolean available = certificateService.isAvailable();
        model.addAttribute("available", available);
        model.addAttribute("users", getActiveUserNames());
        model.addAttribute("projects", projectService.getAllNonCancelledProjects());
        if (available) {
            model.addAttribute("files", certificateService.getFiles());
        }
        return "admin/certificate";
    }

    @PostMapping("/generate")
    public String generate(
            @RequestParam(required = false) String name,
            Model model) {

        List<String> allUserNames = getActiveUserNames();
        CertificateService.CertificateResult result;

        if (name != null && !name.isBlank()) {
            result = certificateService.generate(List.of(name.trim()));
        } else {
            return "redirect:/admin/certificate";
        }

        populateModel(model, allUserNames, result);
        return "admin/certificate";
    }

    @PostMapping("/generate-project")
    public String generateProject(@RequestParam Long projectId, Model model) {
        List<String> names = projectService.getMembersForProject(projectId).stream()
                .map(ProjectAssignment::getUser)
                .map(u -> u.getName() != null ? u.getName() : "")
                .filter(n -> !n.isBlank())
                .distinct()
                .sorted()
                .toList();

        CertificateService.CertificateResult result = names.isEmpty()
                ? new CertificateService.CertificateResult(List.of(), List.of())
                : certificateService.generate(names);

        populateModel(model, getActiveUserNames(), result);
        return "admin/certificate";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) throws IOException {
        byte[] data = certificateService.download(filename);

        String contentType = filename.endsWith(".pdf")
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build());
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private void populateModel(Model model, List<String> userNames,
                               CertificateService.CertificateResult result) {
        model.addAttribute("available", true);
        model.addAttribute("users", userNames);
        model.addAttribute("projects", projectService.getAllNonCancelledProjects());
        model.addAttribute("files", certificateService.getFiles());
        model.addAttribute("result", result);
    }

    private List<String> getActiveUserNames() {
        return employeeService.findAllActive().stream()
                .map(u -> u.getName() != null ? u.getName() : "")
                .filter(n -> !n.isBlank())
                .sorted(Comparator.naturalOrder())
                .distinct()
                .toList();
    }
}
