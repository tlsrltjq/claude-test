package com.eactive.resourcehub.project.repository;

import com.eactive.resourcehub.project.entity.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {

    /** 특정 프로젝트의 전체 배정 목록 (이름순) */
    @Query("SELECT pa FROM ProjectAssignment pa JOIN FETCH pa.user u LEFT JOIN FETCH u.team " +
           "WHERE pa.project.id = :projectId AND pa.status != 'CANCELLED' " +
           "ORDER BY u.name")
    List<ProjectAssignment> findByProjectId(@Param("projectId") Long projectId);

    /** 특정 직원의 전체 배정 이력 (최신순) */
    @Query("SELECT pa FROM ProjectAssignment pa JOIN FETCH pa.user " +
           "WHERE pa.user.id = :userId ORDER BY pa.startDate DESC")
    List<ProjectAssignment> findByUserId(@Param("userId") Long userId);

    /** 해당 월과 기간이 겹치는 배정 (CANCELLED 포함, 필터는 서비스에서) */
    @Query("SELECT pa FROM ProjectAssignment pa JOIN FETCH pa.user u LEFT JOIN FETCH u.team " +
           "WHERE pa.startDate <= :monthEnd AND pa.endDate >= :monthStart " +
           "ORDER BY pa.startDate, u.name")
    List<ProjectAssignment> findForMonth(@Param("monthStart") LocalDate monthStart,
                                          @Param("monthEnd")   LocalDate monthEnd);

    /** 오늘 기준 투입 중인 배정 (대시보드 통계용) */
    @Query("SELECT pa FROM ProjectAssignment pa JOIN FETCH pa.user u " +
           "WHERE pa.status = 'ACTIVE' " +
           "  AND pa.startDate <= :today AND pa.endDate >= :today")
    List<ProjectAssignment> findActiveOn(@Param("today") LocalDate today);

    /** 특정 사용자의 오늘 기준 활성 배정 (인력표 컬럼용) */
    @Query("SELECT pa FROM ProjectAssignment pa " +
           "WHERE pa.user.id = :userId AND pa.status = 'ACTIVE' " +
           "  AND pa.startDate <= :today AND pa.endDate >= :today " +
           "ORDER BY pa.startDate DESC")
    List<ProjectAssignment> findActiveByUserId(@Param("userId") Long userId,
                                                @Param("today")  LocalDate today);

    /** 중복 경고용: 같은 직원의 기간이 겹치는 배정 (CANCELLED 제외, 자기 자신 제외) */
    @Query("SELECT pa FROM ProjectAssignment pa " +
           "WHERE pa.user.id = :userId AND pa.status != 'CANCELLED' " +
           "  AND pa.startDate <= :endDate AND pa.endDate >= :startDate " +
           "  AND (:excludeId IS NULL OR pa.id != :excludeId)")
    List<ProjectAssignment> findOverlapping(@Param("userId")    Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate")   LocalDate endDate,
                                             @Param("excludeId") Long excludeId);

    /** 이번달 시작하는 배정 (대시보드 카드용) */
    @Query("SELECT pa FROM ProjectAssignment pa " +
           "WHERE pa.status != 'CANCELLED' AND pa.startDate BETWEEN :from AND :to")
    List<ProjectAssignment> findStartingBetween(@Param("from") LocalDate from,
                                                  @Param("to")   LocalDate to);

    /** 이번달 종료하는 배정 (대시보드 카드용) */
    @Query("SELECT pa FROM ProjectAssignment pa " +
           "WHERE pa.status != 'CANCELLED' AND pa.endDate BETWEEN :from AND :to")
    List<ProjectAssignment> findEndingBetween(@Param("from") LocalDate from,
                                               @Param("to")   LocalDate to);

    /** 종료 임박 배정: ACTIVE 상태이고 endDate가 today~soonDate 사이 (대시보드 경고 목록용) */
    @Query("SELECT pa FROM ProjectAssignment pa JOIN FETCH pa.user u " +
           "WHERE pa.status = 'ACTIVE' " +
           "  AND pa.endDate BETWEEN :today AND :soonDate " +
           "ORDER BY pa.endDate ASC")
    List<ProjectAssignment> findEndingSoon(@Param("today")    LocalDate today,
                                            @Param("soonDate") LocalDate soonDate);

    /** 다음 투입 예정 배정: PLANNED 상태이고 startDate > today (인력표 '투입 정보' 컬럼용) */
    @Query("SELECT pa FROM ProjectAssignment pa JOIN FETCH pa.user u " +
           "WHERE pa.status = 'PLANNED' AND pa.startDate > :today " +
           "ORDER BY pa.startDate ASC")
    List<ProjectAssignment> findPlannedFrom(@Param("today") LocalDate today);
}
