package com.eactive.resourcehub.certificate;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final EmployeeManagementService employeeService;

    @GetMapping
    public String index(Model model) {
        boolean available = certificateService.isAvailable();
        model.addAttribute("available", available);
        model.addAttribute("users", getActiveUserNames());
        if (available) {
            model.addAttribute("files", certificateService.getFiles());
        }
        return "admin/certificate";
    }

    @PostMapping("/generate")
    public String generate(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> names,
            @RequestParam(defaultValue = "false") boolean all,
            Model model) {

        List<String> allUserNames = getActiveUserNames();

        CertificateService.CertificateResult result;
        if (all) {
            result = certificateService.generateAll(allUserNames);
        } else if (name != null && !name.isBlank()) {
            result = certificateService.generate(List.of(name.trim()));
        } else if (names != null && !names.isEmpty()) {
            result = certificateService.generate(names);
        } else {
            return "redirect:/admin/certificate";
        }

        model.addAttribute("available", true);
        model.addAttribute("users", allUserNames);
        model.addAttribute("files", certificateService.getFiles());
        model.addAttribute("result", result);
        return "admin/certificate";
    }

    @PostMapping("/create")
    public String create(@RequestParam String name, Model model) {
        String message;
        try {
            certificateService.createTemplate(name.trim());
            message = "템플릿 생성 완료: " + name;
        } catch (IOException e) {
            message = "오류: " + e.getMessage();
        }
        model.addAttribute("available", true);
        model.addAttribute("users", getActiveUserNames());
        model.addAttribute("files", certificateService.getFiles());
        model.addAttribute("createMessage", message);
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

    private List<String> getActiveUserNames() {
        return employeeService.findAllActive().stream()
                .map(u -> u.getName() != null ? u.getName() : "")
                .filter(n -> !n.isBlank())
                .sorted(Comparator.naturalOrder())
                .distinct()
                .toList();
    }
}
