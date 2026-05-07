package com.eactive.resourcehub.document.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {
    RESUME("이력서", false),
    CAREER_DESCRIPTION("경력기술서", false),
    GRADUATION_CERTIFICATE("졸업증명서", false),
    LICENSE("정보처리기사", false),
    HEALTH_INSURANCE_PROOF("건강보험료납부확인서", false),
    PROFILE_PHOTO("증명사진", false),
    EMPLOYMENT_CERTIFICATE("재직증명서", true),   // deprecated — 업로드 옵션 제외, 기존 데이터 보존
    ETC("기타", false);

    private final String displayName;
    private final boolean deprecated;

    public boolean isActive() {
        return !deprecated;
    }
}
