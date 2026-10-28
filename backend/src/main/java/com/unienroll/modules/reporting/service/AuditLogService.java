package com.unienroll.modules.reporting.service;

import com.unienroll.modules.reporting.dto.AuditLogResponse;
import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import com.unienroll.modules.reporting.entity.RegistrationAuditLog;
import com.unienroll.modules.reporting.event.RegistrationAuditEvent;
import com.unienroll.modules.reporting.repository.RegistrationAuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final RegistrationAuditLogRepository auditLogRepository;

    /**
     * BR-08: Ghi nhận vết kiểm toán bất đồng bộ qua Spring Event Listener
     */
    @Async
    @EventListener
    @Transactional
    public void handleRegistrationAuditEvent(RegistrationAuditEvent event) {
        try {
            RegistrationAuditLog logEntry = RegistrationAuditLog.builder()
                    .actorId(event.getActorId())
                    .actorUsername(event.getActorUsername())
                    .actionType(event.getActionType())
                    .targetSectionId(event.getTargetSectionId())
                    .targetCourseCode(event.getTargetCourseCode())
                    .timestamp(Instant.now())
                    .ipAddress(event.getIpAddress() != null ? event.getIpAddress() : "127.0.0.1")
                    .executionStatus(event.getExecutionStatus())
                    .errorMessage(event.getErrorMessage())
                    .build();

            auditLogRepository.save(logEntry);
            log.info("Persisted audit log: actor={}, action={}, sectionId={}, status={}",
                    event.getActorUsername(), event.getActionType(), event.getTargetSectionId(), event.getExecutionStatus());
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByActor(Long actorId) {
        return auditLogRepository.findByActorIdOrderByTimestampDesc(actorId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsBySection(Long sectionId) {
        return auditLogRepository.findByTargetSectionIdOrderByTimestampDesc(sectionId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsByAction(AuditActionType actionType) {
        return auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> searchAuditLogs(Long actorId, Long sectionId, AuditActionType actionType,
                                                  AuditExecutionStatus status, Instant startDate, Instant endDate) {
        Specification<RegistrationAuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actorId != null) {
                predicates.add(cb.equal(root.get("actorId"), actorId));
            }
            if (sectionId != null) {
                predicates.add(cb.equal(root.get("targetSectionId"), sectionId));
            }
            if (actionType != null) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("executionStatus"), status));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            query.orderBy(cb.desc(root.get("timestamp")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditLogResponse mapToResponse(RegistrationAuditLog entry) {
        return AuditLogResponse.builder()
                .id(entry.getId())
                .actorId(entry.getActorId())
                .actorUsername(entry.getActorUsername())
                .actionType(entry.getActionType())
                .targetSectionId(entry.getTargetSectionId())
                .targetCourseCode(entry.getTargetCourseCode())
                .timestamp(entry.getTimestamp())
                .ipAddress(entry.getIpAddress())
                .executionStatus(entry.getExecutionStatus())
                .errorMessage(entry.getErrorMessage())
                .build();
    }
}
