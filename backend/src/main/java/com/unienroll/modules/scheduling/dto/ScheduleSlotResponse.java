package com.unienroll.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlotResponse {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private String building;
    private Integer roomCapacity;
    private DayOfWeek dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
}
