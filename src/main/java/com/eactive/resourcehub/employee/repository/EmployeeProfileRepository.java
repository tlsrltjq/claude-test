package com.eactive.resourcehub.employee.repository;

import com.eactive.resourcehub.employee.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    Optional<EmployeeProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT ep FROM EmployeeProfile ep JOIN FETCH ep.user WHERE ep.user.id IN :userIds")
    List<EmployeeProfile> findByUserIdIn(@Param("userIds") List<Long> userIds);
}
