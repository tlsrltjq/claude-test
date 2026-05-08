package com.eactive.resourcehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * eActive Resource Hub — 회사 내부 직원 문서 관리 포털.
 *
 * <p>1단계: 부팅 가능한 골격. 회원가입/로그인/파일 업로드는 다음 단계 이후.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ResourceHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceHubApplication.class, args);
    }
}
