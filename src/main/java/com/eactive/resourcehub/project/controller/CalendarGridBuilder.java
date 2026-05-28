package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.entity.ProjectStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 캘린더 페이지 전용 그리드 빌더. 순수 프레젠테이션 로직 — Spring 의존 없음. */
class CalendarGridBuilder {

    private CalendarGridBuilder() {}

    /**
     * 해당 월의 주(week) 목록을 반환.
     * 각 주는 7개의 LocalDate(null = 해당 월 밖 날짜)로 구성. 일요일 시작.
     * DayOfWeek: MON=1..SUN=7. 일요일 시작 오프셋: SUN→0, MON→1, ..., SAT→6.
     */
    static List<List<LocalDate>> buildWeeks(YearMonth ym) {
        LocalDate firstDay = ym.atDay(1);
        int offset = firstDay.getDayOfWeek().getValue() % 7;

        List<List<LocalDate>> weeks = new ArrayList<>();
        List<LocalDate> week = new ArrayList<>(Collections.nCopies(offset, null));

        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            week.add(ym.atDay(d));
            if (week.size() == 7) {
                weeks.add(new ArrayList<>(week));
                week = new ArrayList<>();
            }
        }
        if (!week.isEmpty()) {
            while (week.size() < 7) week.add(null);
            weeks.add(week);
        }
        return weeks;
    }

    /**
     * 주(week)별 프로젝트 바(bar) 목록 반환.
     * 반환값의 인덱스는 buildWeeks() 결과와 동일한 순서.
     * 각 주 안에서 CANCELLED가 아닌 프로젝트 하나당 ProjectBar 하나를 생성.
     * startCol: 일요일=0 … 토요일=6, colSpan: 해당 주 안에서 차지하는 일수.
     */
    static List<List<ProjectBar>> buildProjectWeekBars(
            List<Project> projects, YearMonth ym) {
        List<List<LocalDate>> weeks = buildWeeks(ym);
        List<List<ProjectBar>> result = new ArrayList<>();

        for (List<LocalDate> week : weeks) {
            LocalDate weekStart = null, weekEnd = null;
            for (LocalDate d : week) {
                if (d != null) {
                    if (weekStart == null) weekStart = d;
                    weekEnd = d;
                }
            }
            if (weekStart == null) { result.add(new ArrayList<>()); continue; }

            List<ProjectBar> bars = new ArrayList<>();
            for (Project p : projects) {
                if (p.getStatus() == ProjectStatus.CANCELLED) continue;
                if (p.getEndDate().isBefore(weekStart)) continue;
                if (p.getStartDate().isAfter(weekEnd)) continue;

                LocalDate barStart = p.getStartDate().isBefore(weekStart) ? weekStart : p.getStartDate();
                LocalDate barEnd   = p.getEndDate().isAfter(weekEnd)      ? weekEnd   : p.getEndDate();

                int startCol = barStart.getDayOfWeek().getValue() % 7; // SUN=0 MON=1..SAT=6
                int endCol   = barEnd.getDayOfWeek().getValue() % 7;
                int colSpan  = endCol - startCol + 1;

                bars.add(new ProjectBar(p, startCol, colSpan));
            }
            result.add(bars);
        }
        return result;
    }

    /**
     * 날짜 → 해당 날짜에 기간이 겹치는 배정 목록.
     * CANCELLED 배정은 제외. 월 경계를 벗어나는 기간은 클리핑.
     */
    static Map<LocalDate, List<ProjectAssignment>> buildDayMap(
            List<ProjectAssignment> assignments, YearMonth ym) {
        Map<LocalDate, List<ProjectAssignment>> map = new LinkedHashMap<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            map.put(ym.atDay(d), new ArrayList<>());
        }
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();

        for (ProjectAssignment pa : assignments) {
            if (pa.getStatus() == AssignmentStatus.CANCELLED) continue;
            LocalDate from = pa.getStartDate().isBefore(monthStart) ? monthStart : pa.getStartDate();
            LocalDate to   = pa.getEndDate().isAfter(monthEnd)      ? monthEnd   : pa.getEndDate();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                List<ProjectAssignment> dayList = map.get(d);
                if (dayList != null) dayList.add(pa);
            }
        }
        return map;
    }
}
