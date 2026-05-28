package com.eactive.resourcehub.project.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Project 엔티티 순수 단위 테스트.
 * 상태 자동 결정, cancel/update 동작, isActiveOn 경계값을 검증한다.
 * 스펙 ID: PA-002 (Project 버전).
 */
class ProjectTest {

    private static final LocalDate TODAY = LocalDate.now();

    // ── create() 상태 자동 결정 ─────────────────────────────────────

    @Test
    void 시작일이_오늘보다_미래이면_PLANNED() {
        Project p = Project.create("P", null, TODAY.plusDays(1), TODAY.plusDays(30), null);
        assertEquals(ProjectStatus.PLANNED, p.getStatus());
    }

    @Test
    void 시작일이_오늘이면_ACTIVE() {
        Project p = Project.create("P", null, TODAY, TODAY.plusDays(30), null);
        assertEquals(ProjectStatus.ACTIVE, p.getStatus());
    }

    @Test
    void 시작일이_과거이면_ACTIVE() {
        Project p = Project.create("P", null, TODAY.minusDays(5), TODAY.plusDays(10), null);
        assertEquals(ProjectStatus.ACTIVE, p.getStatus());
    }

    // ── cancel() ────────────────────────────────────────────────────

    @Test
    void cancel_호출_후_CANCELLED() {
        Project p = Project.create("P", null, TODAY, TODAY.plusDays(30), null);
        p.cancel();
        assertEquals(ProjectStatus.CANCELLED, p.getStatus());
    }

    // ── update() ────────────────────────────────────────────────────

    @Test
    void update_호출_시_모든_필드_반영() {
        Project p = Project.create("원래", "원래사", TODAY, TODAY.plusDays(10), null);
        p.update("변경됨", "변경사", TODAY.plusDays(1), TODAY.plusDays(20), "메모", ProjectStatus.ENDED);

        assertAll(
                () -> assertEquals("변경됨",           p.getName()),
                () -> assertEquals("변경사",            p.getClientName()),
                () -> assertEquals(TODAY.plusDays(1),  p.getStartDate()),
                () -> assertEquals(TODAY.plusDays(20), p.getEndDate()),
                () -> assertEquals("메모",             p.getMemo()),
                () -> assertEquals(ProjectStatus.ENDED, p.getStatus())
        );
    }

    @Test
    void update_clientName_null_허용() {
        Project p = Project.create("P", "원래사", TODAY, TODAY.plusDays(10), null);
        p.update("P", null, TODAY, TODAY.plusDays(10), null, ProjectStatus.ACTIVE);
        assertNull(p.getClientName());
    }

    // ── isActiveOn() — 날짜 경계값 검증 ────────────────────────────

    @Test
    void isActiveOn_시작일_전날_false() {
        Project p = Project.create("P", null, TODAY, TODAY.plusDays(10), null);
        assertFalse(p.isActiveOn(TODAY.minusDays(1)));
    }

    @Test
    void isActiveOn_시작일_당일_true() {
        Project p = Project.create("P", null, TODAY, TODAY.plusDays(10), null);
        assertTrue(p.isActiveOn(TODAY));
    }

    @Test
    void isActiveOn_기간_중간_true() {
        Project p = Project.create("P", null, TODAY.minusDays(5), TODAY.plusDays(5), null);
        assertTrue(p.isActiveOn(TODAY));
    }

    @Test
    void isActiveOn_종료일_당일_true() {
        Project p = Project.create("P", null, TODAY.minusDays(10), TODAY, null);
        assertTrue(p.isActiveOn(TODAY));
    }

    @Test
    void isActiveOn_종료일_다음날_false() {
        Project p = Project.create("P", null, TODAY.minusDays(10), TODAY, null);
        assertFalse(p.isActiveOn(TODAY.plusDays(1)));
    }
}
