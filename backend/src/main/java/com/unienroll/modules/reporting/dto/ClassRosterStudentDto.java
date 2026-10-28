package com.unienroll.modules.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRosterStudentDto {
    private Long studentId;
    private String studentCode;
    private String fullName;
    private String cohort;
    private String major;
    private String email;
    private Instant enrolledAt;
}
