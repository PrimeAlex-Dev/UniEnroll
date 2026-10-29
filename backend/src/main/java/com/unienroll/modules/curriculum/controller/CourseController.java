package com.unienroll.modules.curriculum.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.curriculum.dto.*;
import com.unienroll.modules.curriculum.service.CourseService;
import com.unienroll.modules.curriculum.service.PrerequisiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/courses", "/api/v1/curriculum/courses", "/api/v1/academic/courses"})
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final PrerequisiteService prerequisiteService;

    /**
     * UC-06: Tạo mới Môn học
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-06: Cập nhật thông tin Môn học
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-06: Xem chi tiết Môn học
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-06: Danh sách toàn bộ Môn học
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        List<CourseResponse> response = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-06: Danh sách Môn học theo Khoa
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByDepartment(@PathVariable Long departmentId) {
        List<CourseResponse> response = courseService.getCoursesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-07: Thiết lập Điều kiện Môn học Tiên quyết (Gán môn tiên quyết cho môn mục tiêu)
     */
    @PostMapping("/{id}/prerequisites")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<CourseSummaryDto>>> assignPrerequisites(
            @PathVariable Long id,
            @Valid @RequestBody PrerequisiteAssignmentRequest request
    ) {
        List<CourseSummaryDto> response = prerequisiteService.assignPrerequisites(id, request.getPrerequisiteCourseIds());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-07: Xem danh sách môn tiên quyết của môn học
     */
    @GetMapping("/{id}/prerequisites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseSummaryDto>>> getPrerequisites(@PathVariable Long id) {
        List<CourseSummaryDto> response = prerequisiteService.getPrerequisites(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-07: Hủy quan hệ môn tiên quyết
     */
    @DeleteMapping("/{id}/prerequisites/{prerequisiteCourseId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> removePrerequisite(
            @PathVariable Long id,
            @PathVariable Long prerequisiteCourseId
    ) {
        prerequisiteService.removePrerequisite(id, prerequisiteCourseId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
