package com.eactive.resourcehub.team.service;

import com.eactive.resourcehub.team.entity.Team;
import com.eactive.resourcehub.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamInitializer {

    private final TeamRepository teamRepository;

    private static final List<String> DEFAULT_TEAMS = List.of(
            "개발팀", "영업팀", "기술지원팀", "경영지원팀"
    );

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDefaultTeams() {
        for (String name : DEFAULT_TEAMS) {
            if (!teamRepository.existsByName(name)) {
                teamRepository.save(Team.create(name, null));
                log.info("기본 팀 생성 — name={}", name);
            }
        }
    }
}
