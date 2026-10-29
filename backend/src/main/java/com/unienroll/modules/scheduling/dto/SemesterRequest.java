package com.unienroll.modules.scheduling.dto;

import com.unienroll.modules.scheduling.entity.SemesterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterRequest {

    @NotBlank(message = "Mã học kỳ không được để trống")
    @Size(min = 2, max = 20, message = "Mã học kỳ từ 2 đến 20 ký tự")
    private String code;

    @NotBlank(message = "Tên học kỳ không được để trống")
    @Size(max = 100, message = "Tên học kỳ tối đa 100 ký tự")
    private String name;

    public void setSemesterCode(String semesterCode) {
        if (this.code == null && semesterCode != null) {
            this.code = semesterCode;
        }
    }

    public void setSemesterName(String semesterName) {
        if (this.name == null && semesterName != null) {
            this.name = semesterName;
        }
    }

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @Builder.Default
    private SemesterStatus status = SemesterStatus.PLANNED;
}
