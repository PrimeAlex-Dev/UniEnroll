package com.unienroll.modules.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationPhaseResponse {

    private Long id;
    private Long semesterId;
    private String semesterName;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private String targetCohort;
    private boolean active;
}
