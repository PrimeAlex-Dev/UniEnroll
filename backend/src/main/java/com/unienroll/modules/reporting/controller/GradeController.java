package com.unienroll.modules.reporting.controller;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.reporting.dto.GradeRecordRequest;
import com.unienroll.modules.reporting.dto.GradeRecordResponse;
import com.unienroll.modules.reporting.service.GradeService;
import com.unienroll.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/academic/grades", "/api/v1/student/grades"})
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final StudentProfileRepository studentProfileRepository;

    /**
     * Tra cứu bảng điểm tích lũy của chính sinh viên đang đăng nhập
     */
    @GetMapping({"/my-transcript", "/me"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<GradeRecordResponse>>> getMyTranscript(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StudentProfile student = studentProfileRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy hồ sơ sinh viên tương ứng với tài khoản"));
        List<GradeRecordResponse> response = gradeService.getStudentTranscript(student.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Nhập / Cập nhật điểm cho sinh viên (Cán bộ Đào tạo)
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
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
