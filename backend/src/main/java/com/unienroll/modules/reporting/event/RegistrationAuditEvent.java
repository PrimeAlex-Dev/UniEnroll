package com.unienroll.modules.reporting.event;

import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationAuditEvent {

    private Long actorId;
    private String actorUsername;
    private AuditActionType actionType;
    private Long targetSectionId;
    private String targetCourseCode;
    private String ipAddress;
    private AuditExecutionStatus executionStatus;
    private String errorMessage;
}
