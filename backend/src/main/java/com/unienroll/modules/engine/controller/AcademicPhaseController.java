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
@RequestMapping("/api/v1/academic/registration-phases")
@RequiredArgsConstructor
public class AcademicPhaseController {

    private final RegistrationPhaseService phaseService;

    /**
     * UC-12: Thiết lập Đợt mở cổng Đăng ký học phần (BR-01)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<RegistrationPhaseResponse>> createPhase(@Valid @RequestBody RegistrationPhaseRequest request) {
        RegistrationPhaseResponse response = phaseService.createPhase(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-12: Xem danh sách các đợt mở cổng theo học kỳ
     */
    @GetMapping("/semester/{semesterId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RegistrationPhaseResponse>>> getPhasesBySemester(@PathVariable Long semesterId) {
        List<RegistrationPhaseResponse> response = phaseService.getPhasesBySemester(semesterId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
