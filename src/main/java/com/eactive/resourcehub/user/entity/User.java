package com.eactive.resourcehub.user.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 100)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserStatus status;

    @Column(nullable = false)
    private boolean emailVerified;

    public static User create(String loginId, String encodedPassword, String name,
                               String email, Team team, String position) {
        User user = new User();
        user.loginId = loginId;
        user.password = encodedPassword;
        user.name = name;
        user.email = email;
        user.team = team;
        user.position = position;
        user.role = UserRole.EMPLOYEE;
        user.status = UserStatus.PENDING_EMAIL_VERIFICATION;
        user.emailVerified = false;
        return user;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.status = UserStatus.PENDING_ADMIN_APPROVAL;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void reject() {
        this.status = UserStatus.REJECTED;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void changeTeam(Team team) {
        this.team = team;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
