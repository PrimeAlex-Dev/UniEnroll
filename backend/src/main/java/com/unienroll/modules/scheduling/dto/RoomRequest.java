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
public class RoomRequest {

    @NotBlank(message = "Số phòng không được để trống")
    @Size(max = 30, message = "Số phòng tối đa 30 ký tự")
    private String roomNumber;

    @NotBlank(message = "Tòa nhà không được để trống")
    @Size(max = 50, message = "Tòa nhà tối đa 50 ký tự")
    private String building;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phòng học phải >= 1")
    private Integer capacity;
}
