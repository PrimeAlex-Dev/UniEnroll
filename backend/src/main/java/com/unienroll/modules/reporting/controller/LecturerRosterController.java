package com.unienroll.modules.reporting.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.reporting.dto.ClassRosterResponse;
import com.unienroll.modules.reporting.service.ClassRosterService;
import com.unienroll.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
public class LecturerRosterController {

    private final ClassRosterService classRosterService;

    /**
     * UC-17: XEM DANH SÁCH SINH VIÊN LỚP HỌC PHẦN (CLASS ROSTER)
     */
    @GetMapping({"/sections/{sectionId}/students", "/sections/{sectionId}/roster"})
    @PreAuthorize("hasAnyRole('LECTURER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClassRosterResponse>> getClassRoster(
            @PathVariable Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ClassRosterResponse response = classRosterService.getClassRoster(userDetails.getUser().getId(), sectionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
