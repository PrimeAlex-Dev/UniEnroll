package com.unienroll.modules.reporting.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.reporting.dto.GradeRecordRequest;
import com.unienroll.modules.reporting.dto.GradeRecordResponse;
import com.unienroll.modules.reporting.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    /**
     * Nhập / Cập nhật điểm cho sinh viên (Cán bộ Đào tạo & Admin)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<GradeRecordResponse>> recordGrade(@Valid @RequestBody GradeRecordRequest request) {
        GradeRecordResponse response = gradeService.recordGrade(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * Tra cứu bảng điểm tích lũy của sinh viên
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<GradeRecordResponse>>> getStudentTranscript(@PathVariable Long studentId) {
        List<GradeRecordResponse> response = gradeService.getStudentTranscript(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
