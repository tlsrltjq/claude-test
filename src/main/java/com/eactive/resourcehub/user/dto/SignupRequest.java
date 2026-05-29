package com.eactive.resourcehub.user.dto;

import com.eactive.resourcehub.user.entity.Position;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "생년월일을 입력해주세요.")
    @Pattern(regexp = "^(\\d{8}|\\d{4}\\.\\d{2}\\.\\d{2})$", message = "생년월일 형식이 올바르지 않습니다.")
    private String birthDateStr;

    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^[0-9\\-+() ]{7,20}$", message = "올바른 연락처 형식이 아닙니다.")
    private String phone;

    private String address;

    private String joinDateStr;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    private Long teamId;

    @NotNull(message = "직급을 선택해주세요.")
    private Position position;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    private boolean privacyConsent;
}
