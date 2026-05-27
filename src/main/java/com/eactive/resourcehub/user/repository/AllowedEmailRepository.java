package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.user.entity.AllowedEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AllowedEmailRepository extends JpaRepository<AllowedEmail, Long> {

    boolean existsByEmail(String email);

    Optional<AllowedEmail> findByEmail(String email);

    List<AllowedEmail> findAllByOrderByCreatedAtDesc();
}
