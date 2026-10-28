package com.unienroll.modules.engine.dto;

import com.unienroll.modules.scheduling.dto.CourseSectionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveRegistrationsResponse {

    private Long studentId;
    private String studentCode;
    private String studentName;
    private Long semesterId;
    private String semesterName;
    private Integer totalCredits;
    private List<CourseSectionResponse> enrolledSections;
}
