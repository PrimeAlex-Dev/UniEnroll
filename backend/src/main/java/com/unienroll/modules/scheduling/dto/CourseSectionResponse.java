package com.unienroll.modules.scheduling.dto;

import com.unienroll.modules.scheduling.entity.SectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSectionResponse {

    private Long id;
    private String sectionCode;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer totalCredits;
    private Long semesterId;
    private String semesterName;
    private Long lecturerId;
    private String lecturerName;
    private Integer capacity;
    private Integer enrolledCount;
    private Integer availableSlots;
    private SectionStatus status;
    private List<ScheduleSlotResponse> scheduleSlots;
}
