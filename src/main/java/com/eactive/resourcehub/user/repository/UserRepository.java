package com.eactive.resourcehub.user.repository;

import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.entity.UserRole;
import com.eactive.resourcehub.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.team WHERE u.status IN :statuses")
    List<User> findByStatusInWithTeam(@Param("statuses") List<UserStatus> statuses);

    /**
     * 관리자 직원 목록: 상태·검색어·직급·역할·팀 필터 + DB 페이지네이션.
     * null 파라미터는 해당 조건을 무시한다.
     */
    @Query(value = "SELECT u FROM User u LEFT JOIN u.team t " +
                   "WHERE u.status IN :statuses " +
                   "AND (:qLike IS NULL OR LOWER(u.name) LIKE :qLike OR LOWER(u.email) LIKE :qLike) " +
                   "AND (:position IS NULL OR u.position = :position) " +
                   "AND (:role IS NULL OR u.role = :role) " +
                   "AND (:teamId IS NULL OR t.id = :teamId) " +
                   "ORDER BY u.name",
           countQuery = "SELECT COUNT(u) FROM User u LEFT JOIN u.team t " +
                        "WHERE u.status IN :statuses " +
                        "AND (:qLike IS NULL OR LOWER(u.name) LIKE :qLike OR LOWER(u.email) LIKE :qLike) " +
                        "AND (:position IS NULL OR u.position = :position) " +
                        "AND (:role IS NULL OR u.role = :role) " +
                        "AND (:teamId IS NULL OR t.id = :teamId)")
    Page<User> findFilteredPage(@Param("statuses") List<UserStatus> statuses,
                                @Param("qLike") String qLike,
                                @Param("position") Position position,
                                @Param("role") UserRole role,
                                @Param("teamId") Long teamId,
                                Pageable pageable);

    /**
     * 영업부 인력 목록: ACTIVE 중 ADMIN 제외, 검색어·팀 필터 후 전체 반환 (정렬은 서비스에서).
     * FETCH JOIN 포함 — 호출 측에서 정렬만 수행하면 됨.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.team t " +
           "WHERE u.status = :status AND u.role != :excludeRole " +
           "AND (:qLike IS NULL OR LOWER(u.name) LIKE :qLike OR LOWER(u.email) LIKE :qLike) " +
           "AND (:teamId IS NULL OR t.id = :teamId)")
    List<User> findActiveMembersFiltered(@Param("status") UserStatus status,
                                         @Param("excludeRole") UserRole excludeRole,
                                         @Param("qLike") String qLike,
                                         @Param("teamId") Long teamId);
}
