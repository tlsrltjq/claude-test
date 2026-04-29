package com.eactive.resourcehub.user.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 6)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime verifiedAt;

    public static EmailVerificationToken create(User user, String token, int ttlMinutes) {
        EmailVerificationToken evt = new EmailVerificationToken();
        evt.user = user;
        evt.email = user.getEmail();
        evt.token = token;
        evt.expiredAt = LocalDateTime.now().plusMinutes(ttlMinutes);
        return evt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public void markVerified() {
        this.verifiedAt = LocalDateTime.now();
    }
}
