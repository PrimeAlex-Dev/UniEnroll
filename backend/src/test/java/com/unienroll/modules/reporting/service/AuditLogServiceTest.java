package com.unienroll.modules.reporting.service;

import com.unienroll.modules.reporting.dto.AuditLogResponse;
import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import com.unienroll.modules.reporting.entity.RegistrationAuditLog;
import com.unienroll.modules.reporting.event.RegistrationAuditEvent;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private RegistrationAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    @DisplayName("BR-08: Tiếp nhận Event và lưu trữ bản ghi nhật ký kiểm toán bất biến")
    void shouldPersistAuditLogFromEvent() {
        RegistrationAuditEvent event = RegistrationAuditEvent.builder()
                .actorId(10L)
                .actorUsername("student01")
                .actionType(AuditActionType.ENROLL)
                .targetSectionId(100L)
                .targetCourseCode("CS101")
                .ipAddress("192.168.1.100")
                .executionStatus(AuditExecutionStatus.SUCCESS)
                .build();

        auditLogService.handleRegistrationAuditEvent(event);

        ArgumentCaptor<RegistrationAuditLog> captor = ArgumentCaptor.forClass(RegistrationAuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        RegistrationAuditLog savedLog = captor.getValue();
        assertEquals(10L, savedLog.getActorId());
        assertEquals("student01", savedLog.getActorUsername());
        assertEquals(AuditActionType.ENROLL, savedLog.getActionType());
        assertEquals(100L, savedLog.getTargetSectionId());
        assertEquals("CS101", savedLog.getTargetCourseCode());
        assertEquals(AuditExecutionStatus.SUCCESS, savedLog.getExecutionStatus());
        assertNotNull(savedLog.getTimestamp());
    }

    @Test
    @DisplayName("Truy xuất danh sách nhật ký kiểm toán thành công")
    void shouldRetrieveAuditLogs() {
        RegistrationAuditLog entry = RegistrationAuditLog.builder()
                .id(1L)
                .actorId(10L)
                .actorUsername("student01")
                .actionType(AuditActionType.ENROLL)
                .timestamp(Instant.now())
                .executionStatus(AuditExecutionStatus.SUCCESS)
                .build();

        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(entry));

        List<AuditLogResponse> logs = auditLogService.getAllAuditLogs();

        assertEquals(1, logs.size());
        assertEquals("student01", logs.get(0).getActorUsername());
        assertEquals(AuditActionType.ENROLL, logs.get(0).getActionType());
    }
}
