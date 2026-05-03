package com.eactive.resourcehub.template.repository;

import com.eactive.resourcehub.template.entity.ResumeTemplate;
import com.eactive.resourcehub.template.entity.ResumeTemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeTemplateRepository extends JpaRepository<ResumeTemplate, Long> {

    List<ResumeTemplate> findByStatus(ResumeTemplateStatus status);

    Optional<ResumeTemplate> findFirstByStatusOrderByCreatedAtDesc(ResumeTemplateStatus status);
}
