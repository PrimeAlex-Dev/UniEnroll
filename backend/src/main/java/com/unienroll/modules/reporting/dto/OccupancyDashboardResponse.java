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
public class OccupancyDashboardResponse {
    private Long semesterId;
    private String semesterName;
    private Integer totalSections;
    private Integer totalCapacity;
    private Integer totalEnrolled;
    private Double overallOccupancyRate;
    private List<SectionOccupancyDto> fullSections;
    private List<SectionOccupancyDto> optimalSections;
    private List<SectionOccupancyDto> atRiskSections;
}
