package com.eactive.resourcehub.user.dto;

import com.eactive.resourcehub.user.entity.Position;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveUserRequest {
    private Long teamId;
    private Position position;
}
