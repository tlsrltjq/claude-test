package com.eactive.resourcehub.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 10)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private LocalDateTime verifiedAt;
    private LocalDateTime consumedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static PasswordResetToken create(User user, String token) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.user = user;
        prt.email = user.getEmail();
        prt.token = token;
        prt.expiredAt = LocalDateTime.now().plusMinutes(5);
        prt.createdAt = LocalDateTime.now();
        return prt;
    }

    public boolean isExpired()   { return LocalDateTime.now().isAfter(expiredAt); }
    public boolean isVerified()  { return verifiedAt != null; }
    public boolean isConsumed()  { return consumedAt != null; }

    public void markVerified()  { this.verifiedAt  = LocalDateTime.now(); }
    public void markConsumed()  { this.consumedAt  = LocalDateTime.now(); }
    public void invalidate()    { this.expiredAt   = LocalDateTime.now().minusSeconds(1); }
}
