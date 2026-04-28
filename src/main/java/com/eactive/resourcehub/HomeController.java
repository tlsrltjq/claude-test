package com.eactive.resourcehub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 1단계 동작 확인용 엔드포인트.
 * <p>본격적인 화면(/dashboard, /login 등)은 3단계 이후에 추가된다.
 */
@RestController
public class HomeController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
