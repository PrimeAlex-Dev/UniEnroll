package com.unienroll.modules.scheduling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSectionRequest {

    @NotBlank(message = "Mã lớp học phần không được để trống")
    @Size(min = 2, max = 30, message = "Mã lớp từ 2 đến 30 ký tự")
    private String sectionCode;

    @NotNull(message = "Môn học không được để trống")
    private Long courseId;

    @NotNull(message = "Học kỳ không được để trống")
    private Long semesterId;

    private Long lecturerId;

    @NotNull(message = "Sức chứa lớp học phần không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    public void setMaxCapacity(Integer maxCapacity) {
        if (this.capacity == null && maxCapacity != null) {
            this.capacity = maxCapacity;
        }
    }
}
