package com.eactive.resourcehub.project.entity;

import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProjectAssignment 엔티티 순수 단위 테스트.
 * 날짜 계산, 상태 자동 결정, isActiveOn 경계값을 검증한다.
 * 스펙 ID: PA-002, PA-006.
 */
class ProjectAssignmentTest {

    private static final LocalDate TODAY = LocalDate.now();
    private User user;

    @BeforeEach
    void setUp() {
        user = User.create("emp@test.co.kr", "encoded", "홍길동",
                "emp@test.co.kr", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    // ── create() 상태 자동 결정 ─────────────────────────────────────

    @Test
    void 시작일이_오늘보다_미래이면_PLANNED() {
        ProjectAssignment pa = make(TODAY.plusDays(1), TODAY.plusDays(30));
        assertEquals(AssignmentStatus.PLANNED, pa.getStatus());
    }

    @Test
    void 시작일이_오늘이면_ACTIVE() {
        ProjectAssignment pa = make(TODAY, TODAY.plusDays(30));
        assertEquals(AssignmentStatus.ACTIVE, pa.getStatus());
    }

    @Test
    void 시작일이_과거이면_ACTIVE() {
        ProjectAssignment pa = make(TODAY.minusDays(10), TODAY.plusDays(20));
        assertEquals(AssignmentStatus.ACTIVE, pa.getStatus());
    }

    // ── cancel() ────────────────────────────────────────────────────

    @Test
    void cancel_호출_후_CANCELLED() {
        ProjectAssignment pa = make(TODAY, TODAY.plusDays(30));
        pa.cancel();
        assertEquals(AssignmentStatus.CANCELLED, pa.getStatus());
    }

    // ── update() ────────────────────────────────────────────────────

    @Test
    void update_호출_시_모든_필드_반영() {
        ProjectAssignment pa = make(TODAY, TODAY.plusDays(30));
        pa.update("신프로젝트", "신고객사", "신역할",
                TODAY.plusDays(5), TODAY.plusDays(60),
                AssignmentStatus.ENDED, "신메모");

        assertAll(
                () -> assertEquals("신프로젝트",           pa.getProjectName()),
                () -> assertEquals("신고객사",             pa.getClientName()),
                () -> assertEquals("신역할",               pa.getRole()),
                () -> assertEquals(TODAY.plusDays(5),     pa.getStartDate()),
                () -> assertEquals(TODAY.plusDays(60),    pa.getEndDate()),
                () -> assertEquals(AssignmentStatus.ENDED, pa.getStatus()),
                () -> assertEquals("신메모",               pa.getMemo())
        );
    }

    // ── remainingDays() ─────────────────────────────────────────────

    @Test
    void 잔여일_endDate가_오늘이면_0() {
        ProjectAssignment pa = make(TODAY.minusDays(5), TODAY);
        assertEquals(0, pa.remainingDays());
    }

    @Test
    void 잔여일_endDate가_내일이면_1() {
        ProjectAssignment pa = make(TODAY.minusDays(5), TODAY.plusDays(1));
        assertEquals(1, pa.remainingDays());
    }

    @Test
    void 잔여일_endDate가_이미_지나면_0() {
        ProjectAssignment pa = make(TODAY.minusDays(10), TODAY.minusDays(1));
        assertEquals(0, pa.remainingDays());
    }

    @Test
    void 잔여일_14일이내_강조_대상() {
        ProjectAssignment pa = make(TODAY.minusDays(1), TODAY.plusDays(13));
        long remaining = pa.remainingDays();
        assertTrue(remaining > 0 && remaining <= 14,
                "잔여일 %d이(가) 0초과 14이하여야 합니다".formatted(remaining));
    }

    @Test
    void 잔여일_14일_초과이면_강조_비대상() {
        ProjectAssignment pa = make(TODAY.minusDays(1), TODAY.plusDays(15));
        assertTrue(pa.remainingDays() > 14);
    }

    // ── isActiveOn() — 날짜 경계값 검증 ────────────────────────────

    @Test
    void isActiveOn_시작일_전날_false() {
        ProjectAssignment pa = make(TODAY, TODAY.plusDays(10));
        assertFalse(pa.isActiveOn(TODAY.minusDays(1)));
    }

    @Test
    void isActiveOn_시작일_당일_true() {
        ProjectAssignment pa = make(TODAY, TODAY.plusDays(10));
        assertTrue(pa.isActiveOn(TODAY));
    }

    @Test
    void isActiveOn_기간_중간_true() {
        ProjectAssignment pa = make(TODAY.minusDays(5), TODAY.plusDays(5));
        assertTrue(pa.isActiveOn(TODAY));
    }

    @Test
    void isActiveOn_종료일_당일_true() {
        ProjectAssignment pa = make(TODAY.minusDays(10), TODAY);
        assertTrue(pa.isActiveOn(TODAY));
    }

    @Test
    void isActiveOn_종료일_다음날_false() {
        ProjectAssignment pa = make(TODAY.minusDays(10), TODAY);
        assertFalse(pa.isActiveOn(TODAY.plusDays(1)));
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────

    private ProjectAssignment make(LocalDate start, LocalDate end) {
        return ProjectAssignment.create(user, "테스트 프로젝트", "테스트 고객사",
                "개발자", start, end, null);
    }
}
