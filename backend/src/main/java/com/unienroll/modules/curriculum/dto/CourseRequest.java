package com.unienroll.modules.curriculum.dto;

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
public class CourseRequest {

    @NotBlank(message = "Mã môn học không được để trống")
    @Size(min = 2, max = 30, message = "Mã môn học từ 2 đến 30 ký tự")
    private String code;

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 100, message = "Tên môn học tối đa 100 ký tự")
    private String name;

    @NotNull(message = "Số tín chỉ lý thuyết không được để trống")
    @Min(value = 0, message = "Tín chỉ lý thuyết phải >= 0")
    private Integer theoryCredits;

    @NotNull(message = "Số tín chỉ thực hành không được để trống")
    @Min(value = 0, message = "Tín chỉ thực hành phải >= 0")
    private Integer practicalCredits;

    @NotNull(message = "Khoa/Bộ môn không được để trống")
    private Long departmentId;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
}
