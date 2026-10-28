package com.unienroll.modules.curriculum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Mã khoa không được để trống")
    @Size(min = 2, max = 20, message = "Mã khoa từ 2 đến 20 ký tự")
    private String code;

    @NotBlank(message = "Tên khoa không được để trống")
    @Size(max = 100, message = "Tên khoa tối đa 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
