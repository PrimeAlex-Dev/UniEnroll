package com.unienroll.modules.auth.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.auth.dto.CreateUserRequest;
import com.unienroll.modules.auth.dto.UpdateUserStatusRequest;
import com.unienroll.modules.auth.dto.UserSummaryDto;
import com.unienroll.modules.auth.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * UC-04: Tạo mới tài khoản và phân bổ vai trò (Admin)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserSummaryDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserSummaryDto response = adminUserService.createUser(request);
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    /**
     * UC-04: Danh sách người dùng hệ thống (Admin & Staff)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<java.util.List<UserSummaryDto>>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        java.util.List<UserSummaryDto> response = adminUserService.getAllUsers(role, status, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * UC-04: Cập nhật trạng thái người dùng (Active / Inactive / Locked)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserSummaryDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserSummaryDto response = adminUserService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
