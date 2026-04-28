package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
