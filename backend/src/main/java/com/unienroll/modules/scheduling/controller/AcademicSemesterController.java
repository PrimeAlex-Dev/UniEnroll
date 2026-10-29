package com.unienroll.modules.scheduling.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.scheduling.dto.SemesterRequest;
import com.unienroll.modules.scheduling.dto.SemesterResponse;
import com.unienroll.modules.scheduling.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic/semesters")
@RequiredArgsConstructor
public class AcademicSemesterController {

    private final SemesterService semesterService;

    /**
     * UC-08: Khởi tạo Học kỳ mới
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(@Valid @RequestBody SemesterRequest request) {
        SemesterResponse response = semesterService.createSemester(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-08: Lấy danh sách tất cả các học kỳ
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getAllSemesters() {
        List<SemesterResponse> response = semesterService.getAllSemesters();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-08: Lấy danh sách học kỳ đang hoạt động (ACTIVE)
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getActiveSemesters() {
        List<SemesterResponse> response = semesterService.getActiveSemesters();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-08: Xem chi tiết học kỳ theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemesterById(@PathVariable Long id) {
        SemesterResponse response = semesterService.getSemesterById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
