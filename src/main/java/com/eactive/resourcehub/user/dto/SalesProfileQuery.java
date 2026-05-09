package com.eactive.resourcehub.user.dto;

import com.eactive.resourcehub.user.entity.Position;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SalesProfileQuery {

    private String q;
    private String position;
    private List<String> developerGrades = new ArrayList<>();
    private List<Long> teamIds = new ArrayList<>();
    private String sort = "name";
    private String direction = "asc";
    private String careerDisplay = "ymd"; // ymd | m | d

    public Position positionEnum() {
        if (position == null || position.isBlank()) return null;
        try {
            return Position.valueOf(position);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isSortAsc() {
        return !"desc".equalsIgnoreCase(direction);
    }
}
