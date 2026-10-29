package com.unienroll.modules.scheduling.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.scheduling.dto.*;
import com.unienroll.modules.scheduling.service.CourseSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic/sections")
@RequiredArgsConstructor
public class AcademicSectionController {

    private final CourseSectionService sectionService;

    /**
     * UC-09: Mở Lớp học phần Mới
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> createSection(@Valid @RequestBody CourseSectionRequest request) {
        CourseSectionResponse response = sectionService.createSection(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-10: Cấu hình Lịch học Chi tiết (Schedule Slots) cho Lớp học phần
     */
    @PostMapping("/{id}/schedules")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> assignScheduleSlots(
            @PathVariable Long id,
            @Valid @RequestBody List<ScheduleSlotRequest> slotRequests
    ) {
        CourseSectionResponse response = sectionService.assignScheduleSlots(id, slotRequests);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/slots")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> addSingleScheduleSlot(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleSlotRequest slotRequest
    ) {
        CourseSectionResponse response = sectionService.assignScheduleSlots(id, List.of(slotRequest));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-11: Điều chỉnh Chỉ tiêu Sức chứa Lớp học phần (BR-02 & BR-09)
     */
    @PutMapping("/{id}/capacity")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> updateCapacity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCapacityRequest request
    ) {
        CourseSectionResponse response = sectionService.updateCapacity(id, request.getNewCapacity());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-11: Hủy Lớp học phần
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> cancelSection(@PathVariable Long id) {
        CourseSectionResponse response = sectionService.cancelSection(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Danh sách tất cả Lớp học phần (có thể lọc theo Học kỳ)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getSections(
            @RequestParam(required = false) Long semesterId
    ) {
        List<CourseSectionResponse> response = semesterId != null
                ? sectionService.getSectionsBySemester(semesterId)
                : sectionService.getAllSections();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Xem chi tiết Lớp học phần theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> getSectionById(@PathVariable Long id) {
        CourseSectionResponse response = sectionService.getSectionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Danh sách Lớp học phần theo Học kỳ
     */
    @GetMapping("/semester/{semesterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> getSectionsBySemester(@PathVariable Long semesterId) {
        List<CourseSectionResponse> response = sectionService.getSectionsBySemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
