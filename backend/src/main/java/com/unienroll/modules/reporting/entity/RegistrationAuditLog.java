package com.unienroll.modules.reporting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "registration_audit_logs",
        indexes = {
                @Index(name = "idx_audit_actor", columnList = "actor_id"),
                @Index(name = "idx_audit_section", columnList = "target_section_id"),
                @Index(name = "idx_audit_action", columnList = "action_type"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        }
)
public class RegistrationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "actor_username", nullable = false, length = 50)
    private String actorUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private AuditActionType actionType;

    @Column(name = "target_section_id")
    private Long targetSectionId;

    @Column(name = "target_course_code", length = 30)
    private String targetCourseCode;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 20)
    private AuditExecutionStatus executionStatus;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}
