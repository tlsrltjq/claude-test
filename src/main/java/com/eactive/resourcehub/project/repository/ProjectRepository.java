package com.eactive.resourcehub.project.repository;

import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** 해당 월과 기간이 겹치는 프로젝트 (CANCELLED 포함, 필터는 서비스에서) */
    @Query("SELECT p FROM Project p " +
           "WHERE p.startDate <= :monthEnd AND p.endDate >= :monthStart " +
           "ORDER BY p.startDate, p.name")
    List<Project> findForMonth(@Param("monthStart") LocalDate monthStart,
                               @Param("monthEnd")   LocalDate monthEnd);

    /** 특정 날짜에 진행 중인 프로젝트 */
    @Query("SELECT p FROM Project p " +
           "WHERE p.status != 'CANCELLED' " +
           "  AND p.startDate <= :date AND p.endDate >= :date " +
           "ORDER BY p.startDate")
    List<Project> findActiveOn(@Param("date") LocalDate date);

    List<Project> findByStatusOrderByStartDateAsc(ProjectStatus status);

    /** 취소 제외 전체 프로젝트 (시작일 오름차순) */
    @Query("SELECT p FROM Project p WHERE p.status != 'CANCELLED' ORDER BY p.startDate ASC")
    List<Project> findAllNonCancelled();
}
