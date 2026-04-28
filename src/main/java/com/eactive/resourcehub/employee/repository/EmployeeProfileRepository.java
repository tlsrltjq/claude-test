package com.eactive.resourcehub.employee.repository;

import com.eactive.resourcehub.employee.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    Optional<EmployeeProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
