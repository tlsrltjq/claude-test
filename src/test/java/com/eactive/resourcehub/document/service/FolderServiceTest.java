package com.eactive.resourcehub.document.service;

import com.eactive.resourcehub.audit.entity.AuditActionType;
import com.eactive.resourcehub.audit.entity.AuditTargetType;
import com.eactive.resourcehub.common.service.AuditService;
import com.eactive.resourcehub.document.entity.Folder;
import com.eactive.resourcehub.document.entity.FolderType;
import com.eactive.resourcehub.document.repository.FolderRepository;
import com.eactive.resourcehub.user.entity.Position;
import com.eactive.resourcehub.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock FolderRepository folderRepository;
    @Mock AuditService     auditService;
    @Mock HttpServletRequest httpRequest;

    @InjectMocks FolderService folderService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.create("hong@test.com", "encoded", "홍길동",
                "hong@test.com", null, Position.STAFF,
                LocalDate.of(1990, 1, 1), "010-0000-0000");
        ReflectionTestUtils.setField(owner, "id", 1L);
    }

    @Test
    void 폴더_이미_존재하면_기존_폴더_반환_저장_안_함() {
        Folder existing = Folder.create(owner, "홍길동 개인 폴더");
        ReflectionTestUtils.setField(existing, "id", 10L);
        when(folderRepository.existsByOwnerIdAndType(1L, FolderType.PERSONAL)).thenReturn(true);
        when(folderRepository.findByOwnerIdAndType(1L, FolderType.PERSONAL))
                .thenReturn(Optional.of(existing));

        Folder result = folderService.createPersonalFolder(owner, 1L, httpRequest);

        assertThat(result.getId()).isEqualTo(10L);
        verify(folderRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any());
    }

    @Test
    void 폴더_없으면_새_폴더_저장하고_감사_로그_기록() {
        when(folderRepository.existsByOwnerIdAndType(1L, FolderType.PERSONAL)).thenReturn(false);
        Folder newFolder = Folder.create(owner, "홍길동 개인 폴더");
        ReflectionTestUtils.setField(newFolder, "id", 99L);
        when(folderRepository.save(any())).thenReturn(newFolder);

        Folder result = folderService.createPersonalFolder(owner, 1L, httpRequest);

        assertThat(result.getId()).isEqualTo(99L);
        verify(folderRepository).save(any(Folder.class));
        verify(auditService).log(eq(1L), eq(AuditActionType.CREATE),
                eq(AuditTargetType.FOLDER), eq(99L), anyString(), eq(httpRequest));
    }

    @Test
    void 새_폴더명은_사용자이름_기반() {
        when(folderRepository.existsByOwnerIdAndType(1L, FolderType.PERSONAL)).thenReturn(false);
        when(folderRepository.save(argThat(f -> f.getFolderName().contains("홍길동")))).thenAnswer(inv -> {
            Folder f = inv.getArgument(0);
            ReflectionTestUtils.setField(f, "id", 1L);
            return f;
        });

        folderService.createPersonalFolder(owner, 1L, httpRequest);

        verify(folderRepository).save(argThat(f -> f.getFolderName().contains("홍길동")));
    }
}
