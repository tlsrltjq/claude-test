package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.common.email.EmailSender;
import com.eactive.resourcehub.document.entity.Document;
import com.eactive.resourcehub.document.entity.DocumentType;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.repository.DocumentRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentExpiryServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock EmailSender        emailSender;

    @InjectMocks DocumentExpiryService expiryService;

    private User     owner;
    private Folder   folder;
    private Document expiredDoc;

    @BeforeEach
    void setUp() {
        owner = User.create("hong@test.com", "encoded", "홍길동",
                "hong@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(owner, "id", 1L);

        folder = Folder.create(owner, "개인폴더");
        expiredDoc = Document.create(folder, DocumentType.LICENSE, "자격증");
        expiredDoc.updateExpiresAt(LocalDate.now().minusDays(1));
        ReflectionTestUtils.setField(expiredDoc, "id", 10L);
    }

    // ── findExpired / findExpiringSoon ────────────────────────────

    @Test
    void findExpired_오늘_날짜로_레포지토리_호출() {
        when(documentRepository.findExpired(any())).thenReturn(List.of(expiredDoc));

        var result = expiryService.findExpired();

        assertThat(result).hasSize(1);
        verify(documentRepository).findExpired(LocalDate.now());
    }

    @Test
    void findExpiringSoon_오늘과_30일_후로_레포지토리_호출() {
        when(documentRepository.findExpiringSoon(any(), any())).thenReturn(List.of());
        ArgumentCaptor<LocalDate> todayCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> laterCaptor = ArgumentCaptor.forClass(LocalDate.class);

        expiryService.findExpiringSoon();

        verify(documentRepository).findExpiringSoon(todayCaptor.capture(), laterCaptor.capture());
        assertThat(todayCaptor.getValue()).isEqualTo(LocalDate.now());
        assertThat(laterCaptor.getValue()).isEqualTo(LocalDate.now().plusDays(30));
    }

    // ── sendExpiryNotifications ───────────────────────────────────

    @Test
    void sendExpiryNotifications_만료_문서에_이메일_발송() {
        when(documentRepository.findExpiredNeedingNotice(any())).thenReturn(List.of(expiredDoc));
        when(documentRepository.findExpiringSoonNeedingWarn(any(), any())).thenReturn(List.of());

        expiryService.sendExpiryNotifications();

        verify(emailSender).sendDocumentExpired(eq("hong@test.com"), eq("홍길동"),
                eq("자격증"), any());
    }

    @Test
    void sendExpiryNotifications_발송_성공_시_만료_알림_시각_기록() {
        when(documentRepository.findExpiredNeedingNotice(any())).thenReturn(List.of(expiredDoc));
        when(documentRepository.findExpiringSoonNeedingWarn(any(), any())).thenReturn(List.of());

        expiryService.sendExpiryNotifications();

        assertThat(expiredDoc.getExpiredNoticeSentAt()).isNotNull();
    }

    @Test
    void sendExpiryNotifications_임박_문서에_이메일_발송() {
        Document soonDoc = Document.create(folder, DocumentType.LICENSE, "임박자격증");
        soonDoc.updateExpiresAt(LocalDate.now().plusDays(15));
        ReflectionTestUtils.setField(soonDoc, "id", 20L);

        when(documentRepository.findExpiredNeedingNotice(any())).thenReturn(List.of());
        when(documentRepository.findExpiringSoonNeedingWarn(any(), any())).thenReturn(List.of(soonDoc));

        expiryService.sendExpiryNotifications();

        verify(emailSender).sendDocumentExpiringSoon(eq("hong@test.com"), eq("홍길동"),
                eq("임박자격증"), any(), eq(30));
        assertThat(soonDoc.getExpiryWarnSentAt()).isNotNull();
    }

    @Test
    void sendExpiryNotifications_이메일_실패해도_계속_진행() {
        Document doc2 = Document.create(folder, DocumentType.RESUME, "이력서");
        doc2.updateExpiresAt(LocalDate.now().minusDays(2));
        ReflectionTestUtils.setField(doc2, "id", 30L);

        when(documentRepository.findExpiredNeedingNotice(any())).thenReturn(List.of(expiredDoc, doc2));
        when(documentRepository.findExpiringSoonNeedingWarn(any(), any())).thenReturn(List.of());
        doThrow(new RuntimeException("SMTP error"))
                .when(emailSender).sendDocumentExpired(anyString(), anyString(), anyString(), any());

        expiryService.sendExpiryNotifications();

        verify(emailSender, times(2)).sendDocumentExpired(anyString(), anyString(), anyString(), any());
        // 발송 실패한 문서는 시각을 기록하지 않아 다음 실행에서 재시도된다
        assertThat(expiredDoc.getExpiredNoticeSentAt()).isNull();
        assertThat(doc2.getExpiredNoticeSentAt()).isNull();
    }

    @Test
    void sendExpiryNotifications_대상_없으면_이메일_미발송() {
        when(documentRepository.findExpiredNeedingNotice(any())).thenReturn(List.of());
        when(documentRepository.findExpiringSoonNeedingWarn(any(), any())).thenReturn(List.of());

        expiryService.sendExpiryNotifications();

        verifyNoInteractions(emailSender);
    }
}
