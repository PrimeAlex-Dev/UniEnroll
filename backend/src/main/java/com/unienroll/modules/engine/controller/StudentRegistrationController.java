package com.unienroll.modules.engine.controller;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.engine.dto.ActiveRegistrationsResponse;
import com.unienroll.modules.engine.dto.EnrollmentRequest;
import com.unienroll.modules.engine.dto.EnrollmentResponse;
import com.unienroll.modules.engine.service.RegistrationEngineService;
import com.unienroll.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/registrations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentRegistrationController {

    private final RegistrationEngineService registrationEngineService;
    private final StudentProfileRepository studentProfileRepository;

    /**
     * UC-14: Đăng ký Lớp học phần (Core Transaction)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollmentRequest request,
            HttpServletRequest servletRequest
    ) {
        Long studentId = getStudentId(userDetails);
        String clientIp = servletRequest.getRemoteAddr();

        EnrollmentResponse response = registrationEngineService.enroll(studentId, request.getSectionId(), clientIp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-15: Hủy Đăng ký Lớp học phần (Drop Section)
     */
    @DeleteMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> drop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sectionId,
            HttpServletRequest servletRequest
    ) {
        Long studentId = getStudentId(userDetails);
        String clientIp = servletRequest.getRemoteAddr();

        EnrollmentResponse response = registrationEngineService.drop(studentId, sectionId, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-16: Xem Thời khóa biểu & Danh sách Lớp đã đăng ký thành công
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ActiveRegistrationsResponse>> getActiveRegistrations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long semesterId
    ) {
        Long studentId = getStudentId(userDetails);
        ActiveRegistrationsResponse response = registrationEngineService.getActiveRegistrations(studentId, semesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Long getStudentId(CustomUserDetails userDetails) {
        StudentProfile profile = studentProfileRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy hồ sơ sinh viên tương ứng với tài khoản"));
        return profile.getId();
    }
}
