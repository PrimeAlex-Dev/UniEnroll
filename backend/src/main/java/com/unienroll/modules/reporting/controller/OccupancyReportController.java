package com.unienroll.modules.reporting.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.reporting.dto.OccupancyDashboardResponse;
import com.unienroll.modules.reporting.service.OccupancyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academic/reports")
@RequiredArgsConstructor
public class OccupancyReportController {

    private final OccupancyReportService occupancyReportService;

    /**
     * UC-18: BÁO CÁO THỐNG KÊ TỶ LỆ LẤP ĐẦY LỚP HỌC PHẦN (OCCUPANCY DASHBOARD)
     */
    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OccupancyDashboardResponse>> getOccupancyReport(
            @RequestParam Long semesterId
    ) {
        OccupancyDashboardResponse response = occupancyReportService.getOccupancyDashboard(semesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
