package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);

    List<PasswordResetToken> findByUserIdAndVerifiedAtIsNullAndConsumedAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.expiredAt = CURRENT_TIMESTAMP " +
           "WHERE t.user.id = :userId AND t.verifiedAt IS NULL AND t.consumedAt IS NULL AND t.id <> :excludeId")
    void invalidatePreviousTokens(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}
