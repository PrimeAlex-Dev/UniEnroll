package com.unienroll.modules.reporting.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.reporting.dto.AuditLogResponse;
import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import com.unienroll.modules.reporting.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    /**
     * UC-19: TRA CỨU NHẬT KÝ KIỂM TOÁN ĐĂNG KÝ HỌC PHẦN (AUDIT LOGS INQUIRY)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) AuditExecutionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        List<AuditLogResponse> logs = auditLogService.searchAuditLogs(
                actorId, sectionId, actionType, status, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
