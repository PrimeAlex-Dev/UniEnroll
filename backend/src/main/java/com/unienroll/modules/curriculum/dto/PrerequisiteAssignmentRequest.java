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

    public void setPrerequisiteCourseId(Long prerequisiteCourseId) {
        if (prerequisiteCourseId != null) {
            if (this.prerequisiteCourseIds == null) {
                this.prerequisiteCourseIds = new java.util.ArrayList<>();
            }
            if (!this.prerequisiteCourseIds.contains(prerequisiteCourseId)) {
                this.prerequisiteCourseIds.add(prerequisiteCourseId);
            }
        }
    }
}
