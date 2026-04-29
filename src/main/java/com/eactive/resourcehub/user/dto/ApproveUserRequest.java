package com.eactive.resourcehub.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveUserRequest {
    private Long teamId;
    private String position;
}
