package com.eactive.resourcehub.project.service;

import com.eactive.resourcehub.project.repository.ProjectAssignmentRepository;
import com.eactive.resourcehub.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatusScheduler {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    /** 매일 자정: 날짜 기반으로 프로젝트·배정 상태 자동 전환 */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void syncStatuses() {
        LocalDate today = LocalDate.now();

        int projEnded   = projectRepository.expireActive(today);
        int projActived = projectRepository.activatePlanned(today);
        int paEnded     = assignmentRepository.expireActive(today);
        int paActived   = assignmentRepository.activatePlanned(today);

        if (projEnded + projActived + paEnded + paActived > 0) {
            log.info("상태 자동 전환 — 프로젝트: +{}종료 +{}활성화 / 배정: +{}종료 +{}활성화",
                    projEnded, projActived, paEnded, paActived);
        }
    }
}
