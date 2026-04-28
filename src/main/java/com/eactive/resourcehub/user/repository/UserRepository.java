package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    List<User> findByTeamId(Long teamId);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.team WHERE u.status = :status")
    List<User> findByStatusWithTeam(@Param("status") UserStatus status);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.team WHERE u.id = :id")
    Optional<User> findByIdWithTeam(@Param("id") Long id);
}
