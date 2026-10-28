package com.unienroll.modules.engine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "ID lớp học phần không được để trống")
    private Long sectionId;
}
