package com.unienroll.modules.reporting.dto;

import com.unienroll.modules.scheduling.dto.ScheduleSlotResponse;
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
    private String lecturerEmail;
    private Integer enrolledCount;
    private Integer capacity;
    private List<ScheduleSlotResponse> schedules;
    private List<ClassRosterStudentDto> students;
}
