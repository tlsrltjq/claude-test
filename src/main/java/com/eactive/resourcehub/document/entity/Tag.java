package com.eactive.resourcehub.document.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Tag of(String name) {
        Tag tag = new Tag();
        tag.name = name.trim().toLowerCase();
        tag.createdAt = LocalDateTime.now();
        return tag;
    }
}
