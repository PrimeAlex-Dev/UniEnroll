package com.unienroll.modules.engine.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.engine.dto.RegistrationPhaseRequest;
import com.unienroll.modules.engine.dto.RegistrationPhaseResponse;
import com.unienroll.modules.engine.service.RegistrationPhaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/academic/registration-phases", "/api/v1/academic/phases"})
@RequiredArgsConstructor
public class AcademicPhaseController {

    private final RegistrationPhaseService phaseService;

    /**
     * UC-12: Thiết lập Đợt mở cổng Đăng ký học phần (BR-01)
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<RegistrationPhaseResponse>> createPhase(@Valid @RequestBody RegistrationPhaseRequest request) {
        RegistrationPhaseResponse response = phaseService.createPhase(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-12: Xem danh sách các đợt mở cổng
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RegistrationPhaseResponse>>> getPhases(
            @RequestParam(required = false) Long semesterId
    ) {
        List<RegistrationPhaseResponse> response = semesterId != null
                ? phaseService.getPhasesBySemester(semesterId)
                : phaseService.getAllPhases();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/semester/{semesterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RegistrationPhaseResponse>>> getPhasesBySemester(@PathVariable Long semesterId) {
        List<RegistrationPhaseResponse> response = phaseService.getPhasesBySemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-12: Cập nhật thông tin đợt đăng ký (Gia hạn thời gian)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<RegistrationPhaseResponse>> updatePhase(
            @PathVariable Long id,
            @Valid @RequestBody RegistrationPhaseRequest request
    ) {
        RegistrationPhaseResponse response = phaseService.updatePhase(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-12: Đóng cổng đăng ký khẩn cấp (Force close)
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<RegistrationPhaseResponse>> closePhase(@PathVariable Long id) {
        RegistrationPhaseResponse response = phaseService.closePhase(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-12: Xóa đợt đăng ký chưa bắt đầu
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<Void>> deletePhase(@PathVariable Long id) {
        phaseService.deletePhase(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
