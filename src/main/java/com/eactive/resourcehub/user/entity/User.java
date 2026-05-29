package com.eactive.resourcehub.user.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import com.eactive.resourcehub.team.entity.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Position position;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserStatus status;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 255)
    private String address;

    @Column(name = "join_date")
    private LocalDate joinDate;

    public static User create(String loginId, String encodedPassword, String name,
                               String email, Team team, Position position,
                               LocalDate birthDate, String phone) {
        User user = new User();
        user.loginId = loginId;
        user.password = encodedPassword;
        user.name = name;
        user.email = email;
        user.team = team;
        user.position = position != null ? position : Position.STAFF;
        user.birthDate = birthDate;
        user.phone = phone != null ? phone : "";
        user.role = UserRole.EMPLOYEE;
        user.status = UserStatus.PENDING_EMAIL_VERIFICATION;
        user.emailVerified = false;
        return user;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        this.status = UserStatus.ACTIVE;
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

    public void changePosition(Position position) {
        this.position = position;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String name, String phone, LocalDate birthDate, String address, LocalDate joinDate) {
        if (name != null && !name.isBlank()) this.name = name;
        if (phone != null) this.phone = phone;
        if (birthDate != null) this.birthDate = birthDate;
        if (address != null) this.address = address;
        if (joinDate != null) this.joinDate = joinDate;
    }
}
