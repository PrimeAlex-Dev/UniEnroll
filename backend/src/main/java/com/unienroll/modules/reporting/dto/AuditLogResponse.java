package com.unienroll.modules.reporting.dto;

import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long actorId;
    private String actorUsername;
    private AuditActionType actionType;
    private Long targetSectionId;
    private String targetCourseCode;
    private Instant timestamp;
    private String ipAddress;
    private AuditExecutionStatus executionStatus;
    private String errorMessage;
}
