package com.unienroll.modules.scheduling.entity;

import com.unienroll.common.entity.BaseEntity;
import com.unienroll.modules.auth.entity.LecturerProfile;
import com.unienroll.modules.curriculum.entity.Course;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "course_sections",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_semester_section_code", columnNames = {"semester_id", "section_code"})
        },
        indexes = {
                @Index(name = "idx_section_semester", columnList = "semester_id"),
                @Index(name = "idx_section_course", columnList = "course_id"),
                @Index(name = "idx_section_status", columnList = "status"),
                @Index(name = "idx_section_lecturer", columnList = "lecturer_id")
        }
)
public class CourseSection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, length = 30)
    private String sectionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private LecturerProfile lecturer;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "enrolled_count", nullable = false)
    @Builder.Default
    private Integer enrolledCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SectionStatus status = SectionStatus.OPEN;

    @Builder.Default
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduleSlot> scheduleSlots = new ArrayList<>();
}
