package com.unienroll.modules.scheduling.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlotRequest {

    @NotNull(message = "Phòng học không được để trống")
    private Long roomId;

    @NotNull(message = "Thứ trong tuần không được để trống")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Tiết bắt đầu không được để trống")
    @Min(value = 1, message = "Tiết bắt đầu tối thiểu là 1")
    @Max(value = 12, message = "Tiết bắt đầu tối đa là 12")
    private Integer startPeriod;

    @NotNull(message = "Tiết kết thúc không được để trống")
    @Min(value = 1, message = "Tiết kết thúc tối thiểu là 1")
    @Max(value = 12, message = "Tiết kết thúc tối đa là 12")
    private Integer endPeriod;
}
