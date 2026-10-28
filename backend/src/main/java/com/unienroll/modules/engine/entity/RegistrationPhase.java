package com.unienroll.modules.engine.entity;

import com.unienroll.common.entity.BaseEntity;
import com.unienroll.modules.scheduling.entity.Semester;
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
        name = "registration_phases",
        indexes = {
                @Index(name = "idx_phase_semester", columnList = "semester_id"),
                @Index(name = "idx_phase_time", columnList = "start_time, end_time")
        }
)
public class RegistrationPhase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "target_cohort", length = 30)
    private String targetCohort;
}
