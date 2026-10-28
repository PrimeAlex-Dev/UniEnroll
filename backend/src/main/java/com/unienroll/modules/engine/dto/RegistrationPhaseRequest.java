package com.unienroll.modules.engine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationPhaseRequest {

    @NotNull(message = "ID học kỳ không được để trống")
    private Long semesterId;

    @NotBlank(message = "Tên đợt đăng ký không được để trống")
    @Size(max = 100, message = "Tên đợt tối đa 100 ký tự")
    private String name;

    @NotNull(message = "Thời điểm mở cổng không được để trống")
    private Instant startTime;

    @NotNull(message = "Thời điểm đóng cổng không được để trống")
    private Instant endTime;

    private String targetCohort;
}
