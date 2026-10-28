package com.unienroll.modules.curriculum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String code;
    private String name;
    private Integer theoryCredits;
    private Integer practicalCredits;
    private Integer totalCredits;
    private Long departmentId;
    private String departmentName;
    private String description;
    private List<CourseSummaryDto> prerequisiteCourses;
}
