package com.eactive.resourcehub.template.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.common.file.FileStorage;
import com.eactive.resourcehub.template.entity.ResumeTemplate;
import com.eactive.resourcehub.template.entity.ResumeTemplateStatus;
import com.eactive.resourcehub.template.repository.ResumeTemplateRepository;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.eactive.resourcehub.common.util.FileUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeTemplateService {

    private final ResumeTemplateRepository repository;
    private final FileStorage fileStorage;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private static final long MAX_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "doc", "docx", "hwp", "hwpx");

    @Transactional
    public ResumeTemplate upload(MultipartFile file, Long actorUserId, HttpServletRequest request) {
        validateFile(file);

        // 기존 ACTIVE → ARCHIVED
        repository.findByStatus(ResumeTemplateStatus.ACTIVE)
                .forEach(t -> { t.archive(); repository.save(t); });

        User uploader = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String ext = FileUtils.extension(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "." + ext;
        String storagePath;
        try {
            storagePath = fileStorage.store(file, "resume-templates", storedFileName);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", e);
        }

        ResumeTemplate template = ResumeTemplate.create(
                file.getOriginalFilename(), storedFileName, storagePath,
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                file.getSize(), null, uploader);
        repository.save(template);

        auditService.log(actorUserId, AuditActionType.UPLOAD_RESUME_TEMPLATE,
                AuditTargetType.RESUME_TEMPLATE, template.getId(), null, request);
        return template;
    }

    public Optional<ResumeTemplate> getActive() {
        return repository.findFirstByStatusOrderByCreatedAtDesc(ResumeTemplateStatus.ACTIVE);
    }

    public ResponseEntity<Resource> download(Long actorUserId, HttpServletRequest request) {
        ResumeTemplate template = repository.findFirstByStatusOrderByCreatedAtDesc(ResumeTemplateStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("활성화된 양식 이력서가 없습니다."));

        InputStream is;
        try {
            is = fileStorage.load(template.getStoragePath());
        } catch (IOException e) {
            throw new IllegalStateException("파일을 불러올 수 없습니다.", e);
        }

        auditService.log(actorUserId, AuditActionType.DOWNLOAD_RESUME_TEMPLATE,
                AuditTargetType.RESUME_TEMPLATE, template.getId(), null, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + template.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(template.getContentType()))
                .body(new InputStreamResource(is));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 비어 있습니다.");
        if (file.getSize() > MAX_SIZE) throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
        String ext = FileUtils.extension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + ext);
    }

}
