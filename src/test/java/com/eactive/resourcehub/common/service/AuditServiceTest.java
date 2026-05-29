package com.eactive.resourcehub.common.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditLog;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.audit.repository.AuditLogRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import com.eactive.resourcehub.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditLogRepository auditLogRepository;
    @Mock UserRepository     userRepository;
    @Mock HttpServletRequest httpRequest;

    @InjectMocks AuditService auditService;

    private User makeUser(long id) {
        User u = User.create("user@test.com", "encoded", "테스터",
                "user@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    @Test
    void log_사용자_없으면_저장_안_함() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        auditService.log(99L, AuditActionType.CREATE, AuditTargetType.FOLDER, 1L, null, httpRequest);

        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void log_성공이면_AuditLog_저장() {
        User actor = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("TestBrowser");

        auditService.log(1L, AuditActionType.DELETE, AuditTargetType.DOCUMENT, 5L, "삭제", httpRequest);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getActionType()).isEqualTo(AuditActionType.DELETE);
        assertThat(saved.getTargetType()).isEqualTo(AuditTargetType.DOCUMENT);
        assertThat(saved.getTargetId()).isEqualTo(5L);
        assertThat(saved.getReason()).isEqualTo("삭제");
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(saved.getUserAgent()).isEqualTo("TestBrowser");
    }

    @Test
    void log_XForwardedFor_있으면_첫번째_IP_사용() {
        User actor = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 172.16.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn(null);

        auditService.log(1L, AuditActionType.CREATE, AuditTargetType.FOLDER, 1L, null, httpRequest);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    void log_request_null이면_IP_null_저장() {
        User actor = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(actor));

        auditService.log(1L, AuditActionType.UPDATE, AuditTargetType.USER, 2L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isNull();
        assertThat(captor.getValue().getUserAgent()).isNull();
    }

    @Test
    void log_save_예외발생해도_외부로_전파_안_함() {
        User actor = makeUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(actor));
        when(httpRequest.getHeader(anyString())).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() ->
                auditService.log(1L, AuditActionType.CREATE, AuditTargetType.FOLDER, 1L, null, httpRequest));
    }
}
