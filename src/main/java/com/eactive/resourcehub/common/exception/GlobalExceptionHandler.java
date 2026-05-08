package com.eactive.resourcehub.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException e,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "파일 크기가 20MB를 초과합니다.");
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/my/folder")) {
            return "redirect:/my/folder/documents/upload";
        }
        return "redirect:/";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException e, Model model) {
        int status = e.getStatusCode().value();
        model.addAttribute("message", e.getReason());
        if (status == 403) return "error/403";
        if (status == 404) return "error/404";
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e, HttpServletRequest request, Model model) {
        log.error("Unhandled exception [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return "error/500";
    }
}
