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

    @com.fasterxml.jackson.annotation.JsonSetter("dayOfWeek")
    public void setDayOfWeekRaw(Object value) {
        if (value instanceof DayOfWeek dow) {
            this.dayOfWeek = dow;
        } else if (value instanceof Number num) {
            int n = num.intValue();
            this.dayOfWeek = switch (n) {
                case 1, 8 -> DayOfWeek.SUNDAY;
                case 2 -> DayOfWeek.MONDAY;
                case 3 -> DayOfWeek.TUESDAY;
                case 4 -> DayOfWeek.WEDNESDAY;
                case 5 -> DayOfWeek.THURSDAY;
                case 6 -> DayOfWeek.FRIDAY;
                case 7 -> DayOfWeek.SATURDAY;
                default -> DayOfWeek.MONDAY;
            };
        } else if (value instanceof String str) {
            String s = str.trim().toUpperCase();
            try {
                this.dayOfWeek = DayOfWeek.valueOf(s);
            } catch (IllegalArgumentException e) {
                try {
                    int n = Integer.parseInt(s);
                    this.dayOfWeek = switch (n) {
                        case 1, 8 -> DayOfWeek.SUNDAY;
                        case 2 -> DayOfWeek.MONDAY;
                        case 3 -> DayOfWeek.TUESDAY;
                        case 4 -> DayOfWeek.WEDNESDAY;
                        case 5 -> DayOfWeek.THURSDAY;
                        case 6 -> DayOfWeek.FRIDAY;
                        case 7 -> DayOfWeek.SATURDAY;
                        default -> DayOfWeek.MONDAY;
                    };
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @NotNull(message = "Tiết bắt đầu không được để trống")
    @Min(value = 1, message = "Tiết bắt đầu tối thiểu là 1")
    @Max(value = 12, message = "Tiết bắt đầu tối đa là 12")
    private Integer startPeriod;

    @NotNull(message = "Tiết kết thúc không được để trống")
    @Min(value = 1, message = "Tiết kết thúc tối thiểu là 1")
    @Max(value = 12, message = "Tiết kết thúc tối đa là 12")
    private Integer endPeriod;
}
