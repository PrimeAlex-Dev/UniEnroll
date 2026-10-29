package com.unienroll.modules.scheduling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCapacityRequest {

    @NotNull(message = "Sức chứa mới không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu là 1")
    private Integer newCapacity;

    public void setCapacity(Integer capacity) {
        if (this.newCapacity == null && capacity != null) {
            this.newCapacity = capacity;
        }
    }
}
