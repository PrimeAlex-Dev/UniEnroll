package com.unienroll.modules.reporting.dto;

import com.unienroll.modules.scheduling.entity.SectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionOccupancyDto {
    private Long sectionId;
    private String sectionCode;
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private Integer enrolledCount;
    private Integer capacity;
    private Double occupancyRate;
    private SectionStatus status;
}
