package com.eactive.resourcehub.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "allowed_emails")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AllowedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public static AllowedEmail create(String email, String note, User createdBy) {
        AllowedEmail ae = new AllowedEmail();
        ae.email = email.trim().toLowerCase();
        ae.note = note;
        ae.createdAt = LocalDateTime.now();
        ae.createdBy = createdBy;
        return ae;
    }

    public void updateNote(String note) {
        this.note = note;
    }
}
