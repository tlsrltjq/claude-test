package com.eactive.resourcehub.user.dto;

import com.eactive.resourcehub.user.entity.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    // 이메일 앞부분만 받고 서버에서 도메인을 붙여 풀 이메일 생성
    @NotBlank(message = "이메일 앞부분을 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+$", message = "이메일 앞부분에 잘못된 문자가 포함되어 있습니다.")
    private String emailPrefix;

    @NotNull(message = "직급을 선택해주세요.")
    private Position position;

    @NotNull(message = "생년월일을 입력해주세요.")
    private LocalDate birthDate;

    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^[0-9\\-+() ]{7,20}$", message = "올바른 연락처 형식이 아닙니다.")
    private String phone;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;
}
