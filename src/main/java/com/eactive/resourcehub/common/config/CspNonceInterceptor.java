package com.eactive.resourcehub.common.config;

import com.eactive.resourcehub.common.security.CspNonceFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CspNonceInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView mav) {
        if (mav == null) return;
        String viewName = mav.getViewName();
        if (viewName != null && viewName.startsWith("redirect:")) return;
        mav.addObject("cspNonce", request.getAttribute(CspNonceFilter.NONCE_ATTR));
    }
}
