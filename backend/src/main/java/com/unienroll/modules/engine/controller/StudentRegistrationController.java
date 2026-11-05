package com.unienroll.modules.engine.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.engine.dto.ActiveRegistrationsResponse;
import com.unienroll.modules.engine.dto.EnrollmentRequest;
import com.unienroll.modules.engine.dto.EnrollmentResponse;
import com.unienroll.modules.engine.service.RegistrationEngineService;
import com.unienroll.modules.reporting.dto.ClassRosterResponse;
import com.unienroll.modules.reporting.service.ClassRosterService;
import com.unienroll.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/student/registrations", "/api/v1/registration"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentRegistrationController {

    private final RegistrationEngineService registrationEngineService;
    private final ClassRosterService classRosterService;

    /**
     * UC-14: Đăng ký Lớp học phần (Core Transaction)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollmentRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        EnrollmentResponse response = registrationEngineService.enrollByUserId(userDetails.getId(), request.getSectionId(), clientIp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollAlias(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollmentRequest request,
            HttpServletRequest servletRequest
    ) {
        return enroll(userDetails, request, servletRequest);
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
        String clientIp = servletRequest.getRemoteAddr();
        EnrollmentResponse response = registrationEngineService.dropByUserId(userDetails.getId(), sectionId, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/drop")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> dropAlias(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollmentRequest request,
            HttpServletRequest servletRequest
    ) {
        return drop(userDetails, request.getSectionId(), servletRequest);
    }

    /**
     * UC-16: Xem Thời khóa biểu & Danh sách Lớp đã đăng ký thành công
     */
    @GetMapping({"/active", "/my-schedule"})
    public ResponseEntity<ApiResponse<ActiveRegistrationsResponse>> getActiveRegistrations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long semesterId
    ) {
        ActiveRegistrationsResponse response = registrationEngineService.getActiveRegistrationsByUserId(userDetails.getId(), semesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Xem chi tiết lớp học phần và danh sách bạn học cùng lớp
     */
    @GetMapping({"/sections/{sectionId}/roster", "/sections/{sectionId}/classmates"})
    public ResponseEntity<ApiResponse<ClassRosterResponse>> getSectionRoster(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sectionId
    ) {
        ClassRosterResponse response = classRosterService.getClassRoster(userDetails.getId(), sectionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
