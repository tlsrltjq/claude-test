package com.eactive.resourcehub.project.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProjectAssignmentRequest.validate() 입력 검증 단위 테스트.
 * 스펙 ID: PA-007.
 */
class ProjectAssignmentRequestTest {

    private static final LocalDate TODAY = LocalDate.now();

    // ── 정상 케이스 ─────────────────────────────────────────────────

    @Test
    void 정상_입력이면_예외없음() {
        assertDoesNotThrow(() -> valid().validate());
    }

    @Test
    void allocationRate_경계값_0_허용() {
        ProjectAssignmentRequest req = valid();
        req.setAllocationRate(0);
        assertDoesNotThrow(req::validate);
    }

    @Test
    void allocationRate_경계값_100_허용() {
        ProjectAssignmentRequest req = valid();
        req.setAllocationRate(100);
        assertDoesNotThrow(req::validate);
    }

    @Test
    void startDate와_endDate가_같은날이면_허용() {
        ProjectAssignmentRequest req = valid();
        req.setStartDate(TODAY);
        req.setEndDate(TODAY);
        assertDoesNotThrow(req::validate);
    }

    // ── userId 누락 ─────────────────────────────────────────────────

    @Test
    void userId_null이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setUserId(null);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    // ── projectName 누락·공백 ────────────────────────────────────────

    @Test
    void projectName_null이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setProjectName(null);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    @Test
    void projectName_공백이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setProjectName("   ");
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    // ── 날짜 누락 ────────────────────────────────────────────────────

    @Test
    void startDate_null이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setStartDate(null);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    @Test
    void endDate_null이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setEndDate(null);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    // ── 날짜 순서 오류 ───────────────────────────────────────────────

    @Test
    void endDate가_startDate보다_이전이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setStartDate(TODAY.plusDays(10));
        req.setEndDate(TODAY);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    // ── allocationRate 범위 오류 ────────────────────────────────────

    @Test
    void allocationRate_음수이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setAllocationRate(-1);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    @Test
    void allocationRate_101이면_예외() {
        ProjectAssignmentRequest req = valid();
        req.setAllocationRate(101);
        assertThrows(IllegalArgumentException.class, req::validate);
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────

    private ProjectAssignmentRequest valid() {
        ProjectAssignmentRequest req = new ProjectAssignmentRequest();
        req.setUserId(1L);
        req.setProjectName("테스트 프로젝트");
        req.setStartDate(TODAY);
        req.setEndDate(TODAY.plusDays(30));
        req.setAllocationRate(100);
        return req;
    }
}
