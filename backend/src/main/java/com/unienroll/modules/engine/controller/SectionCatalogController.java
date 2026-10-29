package com.unienroll.modules.engine.controller;

import com.unienroll.common.response.ApiResponse;
import com.unienroll.modules.scheduling.dto.CourseSectionResponse;
import com.unienroll.modules.scheduling.service.CourseSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/sections", "/api/v1/catalog/sections"})
@RequiredArgsConstructor
public class SectionCatalogController {

    private final CourseSectionService sectionService;

    /**
     * UC-13: Tra cứu & Bộ lọc Danh mục Lớp học phần mở (Độ trễ < 200ms)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CourseSectionResponse>>> searchSections(
            @RequestParam Long semesterId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "false") boolean openOnly
    ) {
        List<CourseSectionResponse> sections = sectionService.getSectionsBySemester(semesterId);

        // Áp dụng bộ lọc bộ nhớ nhanh
        List<CourseSectionResponse> filtered = sections.stream()
                .filter(s -> {
                    if (openOnly && (s.getAvailableSlots() <= 0 || s.getStatus().name().equals("CLOSED"))) {
                        return false;
                    }
                    if (StringUtils.hasText(keyword)) {
                        String kw = keyword.toLowerCase().trim();
                        boolean matchCode = s.getSectionCode() != null && s.getSectionCode().toLowerCase().contains(kw);
                        boolean matchCourseCode = s.getCourseCode() != null && s.getCourseCode().toLowerCase().contains(kw);
                        boolean matchCourseName = s.getCourseName() != null && s.getCourseName().toLowerCase().contains(kw);
                        if (!matchCode && !matchCourseCode && !matchCourseName) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(filtered));
    }

    /**
     * Xem chi tiết một Lớp học phần
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseSectionResponse>> getSectionById(@PathVariable Long id) {
        CourseSectionResponse response = sectionService.getSectionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
