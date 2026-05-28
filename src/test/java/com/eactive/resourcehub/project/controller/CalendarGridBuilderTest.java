package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.project.entity.AssignmentStatus;
import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.project.entity.ProjectStatus;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CalendarGridBuilder 그리드 빌더 단위 테스트.
 * 일요일 시작 오프셋, 월 경계 클리핑, CANCELLED 제외를 검증한다.
 * 스펙 ID: PA-003, PA-008.
 */
class CalendarGridBuilderTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create("test@test.co.kr", "encoded", "테스터",
                "test@test.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", 99L);
    }

    // ── buildWeeks — 요일 오프셋 ────────────────────────────────────

    @Test
    void 월_1일이_일요일이면_오프셋_0() {
        // 2025-06-01 = 일요일
        YearMonth ym = YearMonth.of(2025, 6);
        List<List<LocalDate>> weeks = CalendarGridBuilder.buildWeeks(ym);

        // 첫 셀이 null 없이 6월 1일
        assertNotNull(weeks.get(0).get(0));
        assertEquals(LocalDate.of(2025, 6, 1), weeks.get(0).get(0));
    }

    @Test
    void 월_1일이_월요일이면_오프셋_1() {
        // 2025-09-01 = 월요일
        YearMonth ym = YearMonth.of(2025, 9);
        List<List<LocalDate>> weeks = CalendarGridBuilder.buildWeeks(ym);

        // 첫 셀(일요일 자리)은 null, 두 번째 셀이 9월 1일
        assertNull(weeks.get(0).get(0));
        assertEquals(LocalDate.of(2025, 9, 1), weeks.get(0).get(1));
    }

    @Test
    void 월_1일이_토요일이면_오프셋_6() {
        // 2025-02-01 = 토요일
        YearMonth ym = YearMonth.of(2025, 2);
        List<List<LocalDate>> weeks = CalendarGridBuilder.buildWeeks(ym);

        // 앞 6칸(일~금)이 null
        for (int i = 0; i < 6; i++) {
            assertNull(weeks.get(0).get(i), "index " + i + " 는 null이어야 합니다");
        }
        assertEquals(LocalDate.of(2025, 2, 1), weeks.get(0).get(6));
    }

    @Test
    void 각_주는_항상_7개_셀() {
        for (YearMonth ym : List.of(
                YearMonth.of(2025, 1),
                YearMonth.of(2025, 2),
                YearMonth.of(2025, 6),
                YearMonth.of(2025, 12))) {
            CalendarGridBuilder.buildWeeks(ym)
                    .forEach(week -> assertEquals(7, week.size(),
                            ym + " 의 주 크기는 7이어야 합니다"));
        }
    }

    @Test
    void 이십팔일_달_일요일_시작이면_정확히_4주() {
        // 2015-02-01 = 일요일, 28일 → 4주 정확
        YearMonth ym = YearMonth.of(2015, 2);
        assertEquals(4, CalendarGridBuilder.buildWeeks(ym).size());
    }

    @Test
    void 월의_모든_날짜가_정확히_한_번_포함됨() {
        YearMonth ym = YearMonth.of(2025, 3);
        List<List<LocalDate>> weeks = CalendarGridBuilder.buildWeeks(ym);

        long dayCount = weeks.stream()
                .flatMap(List::stream)
                .filter(d -> d != null && d.getMonthValue() == 3)
                .count();
        assertEquals(31, dayCount, "3월은 31일이어야 합니다");
    }

    // ── buildDayMap — 배정 매핑 ────────────────────────────────────

    @Test
    void CANCELLED_배정은_dayMap에_포함되지_않음() {
        YearMonth ym = YearMonth.of(2025, 6);
        ProjectAssignment cancelled = make(
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));
        cancelled.cancel(); // CANCELLED로 전환

        Map<LocalDate, List<ProjectAssignment>> dayMap =
                CalendarGridBuilder.buildDayMap(List.of(cancelled), ym);

        dayMap.values().forEach(list ->
                assertTrue(list.isEmpty(), "CANCELLED 배정은 dayMap에 없어야 합니다"));
    }

    @Test
    void 배정_시작일이_월_이전이면_1일부터_클리핑() {
        YearMonth ym = YearMonth.of(2025, 6);
        // 5월 15일 시작 → 6월 10일 종료
        ProjectAssignment pa = make(
                LocalDate.of(2025, 5, 15), LocalDate.of(2025, 6, 10));

        Map<LocalDate, List<ProjectAssignment>> dayMap =
                CalendarGridBuilder.buildDayMap(List.of(pa), ym);

        assertTrue(dayMap.get(LocalDate.of(2025, 6, 1)).contains(pa),  "6월 1일 포함");
        assertTrue(dayMap.get(LocalDate.of(2025, 6, 10)).contains(pa), "6월 10일 포함");
        assertFalse(dayMap.get(LocalDate.of(2025, 6, 11)).contains(pa), "6월 11일 미포함");
    }

    @Test
    void 배정_종료일이_월_이후이면_말일까지_클리핑() {
        YearMonth ym = YearMonth.of(2025, 6);
        // 6월 20일 시작 → 7월 31일 종료
        ProjectAssignment pa = make(
                LocalDate.of(2025, 6, 20), LocalDate.of(2025, 7, 31));

        Map<LocalDate, List<ProjectAssignment>> dayMap =
                CalendarGridBuilder.buildDayMap(List.of(pa), ym);

        assertTrue(dayMap.get(LocalDate.of(2025, 6, 20)).contains(pa), "6월 20일 포함");
        assertTrue(dayMap.get(LocalDate.of(2025, 6, 30)).contains(pa), "6월 말일 포함");
    }

    @Test
    void 배정이_월_전체를_감싸면_모든_날짜에_포함() {
        YearMonth ym = YearMonth.of(2025, 6);
        ProjectAssignment pa = make(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        Map<LocalDate, List<ProjectAssignment>> dayMap =
                CalendarGridBuilder.buildDayMap(List.of(pa), ym);

        dayMap.values().forEach(list ->
                assertTrue(list.contains(pa), "월 전체 날짜에 배정이 포함되어야 합니다"));
    }

    @Test
    void ENDED_배정은_dayMap에_포함됨() {
        YearMonth ym = YearMonth.of(2025, 6);
        ProjectAssignment pa = make(
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 15));
        // ENDED는 그리드에 표시 (CANCELLED만 제외)
        pa.updateMember(pa.getRole(), pa.getStartDate(), pa.getEndDate(), AssignmentStatus.ENDED);

        Map<LocalDate, List<ProjectAssignment>> dayMap =
                CalendarGridBuilder.buildDayMap(List.of(pa), ym);

        assertTrue(dayMap.get(LocalDate.of(2025, 6, 1)).contains(pa),
                "ENDED 배정은 dayMap에 포함되어야 합니다");
    }

    // ── buildProjectWeekBars ────────────────────────────────────────

    @Test
    void CANCELLED_프로젝트는_weekBars에_포함되지_않음() {
        // 2025-06-01 = 일요일 → 1주차에 6월 전체 포함
        YearMonth ym = YearMonth.of(2025, 6);
        Project cancelled = Project.create("취소됨", null,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30), null);
        cancelled.cancel();

        List<List<ProjectBar>> bars = CalendarGridBuilder.buildProjectWeekBars(List.of(cancelled), ym);

        bars.forEach(weekBars ->
                assertTrue(weekBars.isEmpty(), "CANCELLED 프로젝트는 weekBars에 없어야 합니다"));
    }

    @Test
    void 프로젝트가_주_전체를_감싸면_startCol_0_colSpan_7() {
        // 2025-06-01(일)~2025-06-07(토) = 1주차 전체
        YearMonth ym = YearMonth.of(2025, 6);
        Project p = Project.create("P", null,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null);

        List<List<ProjectBar>> bars = CalendarGridBuilder.buildProjectWeekBars(List.of(p), ym);

        ProjectBar firstWeekBar = bars.get(0).get(0);
        assertAll(
                () -> assertEquals(0, firstWeekBar.startCol(), "일요일 시작 → startCol=0"),
                () -> assertEquals(7, firstWeekBar.colSpan(), "주 전체 → colSpan=7")
        );
    }

    @Test
    void 프로젝트_시작일이_주_중간이면_정확한_startCol() {
        // 2025-09-01 = 월요일 → 1주차는 null(일) + 9/1(월)~9/6(토)
        YearMonth ym = YearMonth.of(2025, 9);
        Project p = Project.create("P", null,
                LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 30), null);

        List<List<ProjectBar>> bars = CalendarGridBuilder.buildProjectWeekBars(List.of(p), ym);

        ProjectBar bar = bars.get(0).get(0);
        assertAll(
                () -> assertEquals(1, bar.startCol(), "월요일 시작 → startCol=1"),
                () -> assertEquals(6, bar.colSpan(), "월~토(6일) → colSpan=6")
        );
    }

    @Test
    void 월_밖에서_시작하는_프로젝트는_월_첫날부터_클리핑() {
        // 2025-06-01(일) 시작 주: 5월에 시작한 프로젝트가 6월 15일에 끝남
        YearMonth ym = YearMonth.of(2025, 6);
        Project p = Project.create("P", null,
                LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 15), null);

        List<List<ProjectBar>> bars = CalendarGridBuilder.buildProjectWeekBars(List.of(p), ym);

        ProjectBar firstWeekBar = bars.get(0).get(0);
        assertEquals(0, firstWeekBar.startCol(), "6/1(일) 시작으로 클리핑 → startCol=0");
        assertEquals(7, firstWeekBar.colSpan(), "첫 주 전체 → colSpan=7");
    }

    @Test
    void 해당_주에_겹치지_않는_프로젝트는_해당_주_bars에_없음() {
        // 2025-06-01 일요일 기준 첫째 주: 6/1~6/7
        // 6월 15일~30일 프로젝트는 첫째 주에 없어야 함
        YearMonth ym = YearMonth.of(2025, 6);
        Project p = Project.create("P", null,
                LocalDate.of(2025, 6, 15), LocalDate.of(2025, 6, 30), null);

        List<List<ProjectBar>> bars = CalendarGridBuilder.buildProjectWeekBars(List.of(p), ym);

        assertTrue(bars.get(0).isEmpty(), "6/15 시작 프로젝트는 1주차 bars에 없어야 합니다");
    }

    @Test
    void 같은_주에_여러_프로젝트_모두_포함() {
        YearMonth ym = YearMonth.of(2025, 6);
        Project p1 = Project.create("P1", null,
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 3), null);
        Project p2 = Project.create("P2", null,
                LocalDate.of(2025, 6, 4), LocalDate.of(2025, 6, 7), null);

        List<List<ProjectBar>> bars = CalendarGridBuilder.buildProjectWeekBars(List.of(p1, p2), ym);

        assertEquals(2, bars.get(0).size(), "1주차에 두 프로젝트 모두 포함되어야 합니다");
    }

    @Test
    void 주_수와_buildWeeks_결과_크기가_일치() {
        for (YearMonth ym : List.of(
                YearMonth.of(2025, 1), YearMonth.of(2025, 6), YearMonth.of(2025, 12))) {
            int weekCount = CalendarGridBuilder.buildWeeks(ym).size();
            int barCount  = CalendarGridBuilder.buildProjectWeekBars(List.of(), ym).size();
            assertEquals(weekCount, barCount,
                    ym + " 의 weekBars 크기는 buildWeeks 크기와 같아야 합니다");
        }
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────

    private ProjectAssignment make(LocalDate start, LocalDate end) {
        Project project = Project.create("테스트 프로젝트", "테스트 고객사", start, end, null);
        return ProjectAssignment.createForProject(project, user, "개발자", start, end);
    }
}
