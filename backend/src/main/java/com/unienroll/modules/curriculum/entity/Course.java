package com.unienroll.modules.curriculum.entity;

import com.unienroll.common.entity.BaseEntity;
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
@Table(name = "courses", indexes = {
        @Index(name = "idx_course_code", columnList = "code"),
        @Index(name = "idx_course_department", columnList = "department_id")
})
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "theory_credits", nullable = false)
    @Builder.Default
    private Integer theoryCredits = 0;

    @Column(name = "practical_credits", nullable = false)
    @Builder.Default
    private Integer practicalCredits = 0;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Builder.Default
    @OneToMany(mappedBy = "targetCourse", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PrerequisiteCondition> prerequisites = new ArrayList<>();
}
