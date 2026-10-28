package com.unienroll.modules.curriculum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryDto {

    private Long id;
    private String code;
    private String name;
    private Integer totalCredits;
}
