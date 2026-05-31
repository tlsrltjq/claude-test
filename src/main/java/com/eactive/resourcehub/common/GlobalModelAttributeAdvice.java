package com.eactive.resourcehub.common;

import com.eactive.resourcehub.common.security.CustomUserDetails;
import com.eactive.resourcehub.user.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser() : null;
    }
}
