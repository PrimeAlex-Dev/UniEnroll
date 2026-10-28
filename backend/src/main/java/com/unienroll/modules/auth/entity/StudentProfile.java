package com.unienroll.modules.auth.entity;

import com.unienroll.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "student_profiles", indexes = {
        @Index(name = "idx_student_code", columnList = "student_code")
})
public class StudentProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_code", nullable = false, unique = true, length = 30)
    private String studentCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "cohort", nullable = false, length = 20)
    private String cohort;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "credit_limit_override")
    @Builder.Default
    private Boolean isCreditLimitOverridden = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
