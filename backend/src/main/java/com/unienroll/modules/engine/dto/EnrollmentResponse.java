package com.unienroll.modules.engine.dto;

import com.unienroll.modules.engine.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long enrollmentId;
    private Long studentId;
    private String studentCode;
    private Long sectionId;
    private String sectionCode;
    private String courseCode;
    private String courseName;
    private Integer totalCredits;
    private EnrollmentStatus status;
    private Instant enrolledAt;
    private Instant droppedAt;
}
