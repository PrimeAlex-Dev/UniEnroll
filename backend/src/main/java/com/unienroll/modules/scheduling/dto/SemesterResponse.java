package com.unienroll.modules.scheduling.dto;

import com.unienroll.modules.scheduling.entity.SemesterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResponse {

    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private SemesterStatus status;

    public String getSemesterCode() {
        return this.code;
    }

    public String getSemesterName() {
        return this.name;
    }
}
