package com.unienroll.modules.reporting.dto;

import com.unienroll.modules.reporting.entity.GradeStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRecordRequest {

    @NotNull(message = "ID sinh viên không được để trống")
    private Long studentId;

    @NotNull(message = "ID môn học không được để trống")
    private Long courseId;

    @NotNull(message = "ID học kỳ không được để trống")
    private Long semesterId;

    @DecimalMin(value = "0.0", message = "Điểm số tối thiểu là 0.0")
    @DecimalMax(value = "10.0", message = "Điểm số tối đa là 10.0")
    private Double finalScore;

    @NotNull(message = "Trạng thái đạt/trượt không được để trống")
    private GradeStatus status;
}
