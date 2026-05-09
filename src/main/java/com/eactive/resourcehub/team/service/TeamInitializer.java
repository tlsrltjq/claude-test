package com.eactive.resourcehub.team.service;

import com.eactive.resourcehub.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamInitializer {

    private final TeamRepository teamRepository;

    // 기본 팀 자동 시딩 제거 — 팀은 관리자가 직접 생성·관리
    // (이전 기본값: 개발팀, 영업팀, 기술지원팀, 경영지원팀)
}
