package com.eactive.resourcehub.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidation(MethodArgumentNotValidException e,
                                   HttpServletResponse response, Model model) {
        if (response.isCommitted()) return null;
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");
        response.setStatus(400);
        model.addAttribute("message", message);
        return "error/500";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException e,
                                         HttpServletResponse response, Model model) {
        if (response.isCommitted()) return null;
        response.setStatus(404);
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException e,
                                       Model model,
                                       HttpServletResponse response) {
        if (response.isCommitted()) return null;
        int status = e.getStatusCode().value();
        response.setStatus(status);
        model.addAttribute("message", e.getReason());
        if (status == 403) return "error/403";
        if (status == 404) return "error/404";
        return "error/500";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResource(NoResourceFoundException e, HttpServletResponse response, Model model) {
        if (response.isCommitted()) return null;
        response.setStatus(404);
        model.addAttribute("message", "페이지를 찾을 수 없습니다.");
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                Model model) {
        if (response.isCommitted()) {
            log.debug("Response already committed, skipping error page [{}]", request.getRequestURI());
            return null;
        }
        log.error("Unhandled exception [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        response.setStatus(500);
        return "error/500";
    }
}
