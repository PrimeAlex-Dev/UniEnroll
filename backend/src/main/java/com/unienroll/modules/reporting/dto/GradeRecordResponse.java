package com.unienroll.modules.reporting.dto;

import com.unienroll.modules.reporting.entity.GradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRecordResponse {

    private Long id;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer totalCredits;
    private Long semesterId;
    private String semesterName;
    private Double finalScore;
    private GradeStatus status;
}
