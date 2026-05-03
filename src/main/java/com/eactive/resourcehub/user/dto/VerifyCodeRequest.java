package com.eactive.resourcehub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeRequest {

    @NotBlank(message = "인증 코드를 입력해주세요.")
    @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자입니다.")
    private String code;
}
