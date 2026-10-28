package com.unienroll.modules.engine.entity;

import com.unienroll.common.entity.BaseEntity;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.scheduling.entity.CourseSection;
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
        name = "enrollment_records",
        indexes = {
                @Index(name = "idx_enroll_student", columnList = "student_id"),
                @Index(name = "idx_enroll_section", columnList = "section_id"),
                @Index(name = "idx_enroll_course_semester", columnList = "course_id, semester_id"),
                @Index(name = "idx_enroll_status", columnList = "status")
        }
)
public class EnrollmentRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "dropped_at")
    private Instant droppedAt;
}
