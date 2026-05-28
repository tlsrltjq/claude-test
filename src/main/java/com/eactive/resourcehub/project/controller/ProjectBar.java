package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.project.entity.Project;

/**
 * 캘린더 주(week) 행 안에서 프로젝트 바(bar) 하나를 나타내는 DTO.
 * startCol: 0(일) ~ 6(토), colSpan: 해당 주 안에서 차지하는 일수.
 */
record ProjectBar(Project project, int startCol, int colSpan) {}
