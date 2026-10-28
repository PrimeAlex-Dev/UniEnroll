package com.unienroll.modules.curriculum.entity;

import com.unienroll.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "prerequisite_conditions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_target_prerequisite",
                        columnNames = {"target_course_id", "prerequisite_course_id"}
                )
        },
        indexes = {
                @Index(name = "idx_target_course", columnList = "target_course_id"),
                @Index(name = "idx_prerequisite_course", columnList = "prerequisite_course_id")
        }
)
public class PrerequisiteCondition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_course_id", nullable = false)
    private Course targetCourse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;
}
