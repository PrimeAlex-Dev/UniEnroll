package com.unienroll.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private String building;
    private Integer capacity;
}
