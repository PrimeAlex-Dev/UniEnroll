package com.unienroll.modules.reporting.entity;

import com.unienroll.common.entity.BaseEntity;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.scheduling.entity.Semester;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "grade_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_student_course_semester_grade", columnNames = {"student_id", "course_id", "semester_id"})
        },
        indexes = {
                @Index(name = "idx_grade_student", columnList = "student_id"),
                @Index(name = "idx_grade_course", columnList = "course_id"),
                @Index(name = "idx_grade_status", columnList = "status")
        }
)
public class GradeRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "final_score")
    private Double finalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GradeStatus status;
}
