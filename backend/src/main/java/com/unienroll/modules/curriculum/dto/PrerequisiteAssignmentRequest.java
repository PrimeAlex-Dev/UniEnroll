package com.unienroll.modules.curriculum.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrerequisiteAssignmentRequest {

    @NotEmpty(message = "Danh sách môn tiên quyết không được để trống")
    private List<Long> prerequisiteCourseIds;
}
