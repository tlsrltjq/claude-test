package com.eactive.resourcehub.team.entity;

import com.eactive.resourcehub.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    public static Team create(String name, String description) {
        Team team = new Team();
        team.name = name;
        team.description = description;
        return team;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
