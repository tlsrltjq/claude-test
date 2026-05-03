package com.eactive.resourcehub.user.dto;

import com.eactive.resourcehub.user.entity.Position;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesProfileQuery {

    private String q;
    private String position;        // Position enum name; null/blank = all
    private String developerGrade;
    private String sort = "name";   // name | position | career | age
    private String direction = "asc";

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
