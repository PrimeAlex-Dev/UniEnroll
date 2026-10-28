package com.unienroll.modules.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRosterResponse {
    private Long sectionId;
    private String sectionCode;
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private Integer enrolledCount;
    private Integer capacity;
    private List<ClassRosterStudentDto> students;
}
