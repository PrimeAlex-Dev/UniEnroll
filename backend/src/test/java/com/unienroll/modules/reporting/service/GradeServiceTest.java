package com.unienroll.modules.reporting.service;

import com.unienroll.modules.curriculum.dto.CourseSummaryDto;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import com.unienroll.modules.reporting.entity.GradeStatus;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRecordRepository gradeRecordRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PrerequisiteConditionRepository prerequisiteRepository;

    @InjectMocks
    private GradeService gradeService;

    private Course course101;
    private Course course102;
    private Course course201;

    @BeforeEach
    void setUp() {
        course101 = Course.builder().id(101L).code("CS101").name("Nhập môn Lập trình").totalCredits(3).build();
        course102 = Course.builder().id(102L).code("CS102").name("Cấu trúc Dữ liệu").totalCredits(4).build();
        course201 = Course.builder().id(201L).code("CS201").name("Thuật toán Nâng cao").totalCredits(4).build();
    }

    @Test
    @DisplayName("BR-04: Môn học không có điều kiện tiên quyết -> hasPassedAllPrerequisites trả về true")
    void shouldReturnTrueWhenNoPrerequisites() {
        when(prerequisiteRepository.findByTargetCourseId(101L)).thenReturn(List.of());

        boolean result = gradeService.hasPassedAllPrerequisites(1L, 101L);

        assertTrue(result);
        List<CourseSummaryDto> missing = gradeService.getMissingPrerequisites(1L, 101L);
        assertTrue(missing.isEmpty());
    }

    @Test
    @DisplayName("BR-04: Sinh viên đã đạt (PASSED) môn tiên quyết -> hasPassedAllPrerequisites trả về true")
    void shouldReturnTrueWhenPrerequisitePassed() {
        PrerequisiteCondition condition = PrerequisiteCondition.builder()
                .targetCourse(course102)
                .prerequisiteCourse(course101)
                .build();
        when(prerequisiteRepository.findByTargetCourseId(102L)).thenReturn(List.of(condition));
        when(gradeRecordRepository.existsByStudentIdAndCourseIdAndStatus(1L, 101L, GradeStatus.PASSED)).thenReturn(true);

        boolean result = gradeService.hasPassedAllPrerequisites(1L, 102L);

        assertTrue(result);
        List<CourseSummaryDto> missing = gradeService.getMissingPrerequisites(1L, 102L);
        assertTrue(missing.isEmpty());
    }

    @Test
    @DisplayName("BR-04: Sinh viên chưa đạt (FAILED) môn tiên quyết -> trả về false kèm mã môn thiếu")
    void shouldReturnFalseWhenPrerequisiteFailed() {
        PrerequisiteCondition condition = PrerequisiteCondition.builder()
                .targetCourse(course102)
                .prerequisiteCourse(course101)
                .build();
        when(prerequisiteRepository.findByTargetCourseId(102L)).thenReturn(List.of(condition));
        // Sinh viên chưa đạt CS101
        when(gradeRecordRepository.existsByStudentIdAndCourseIdAndStatus(1L, 101L, GradeStatus.PASSED)).thenReturn(false);

        boolean result = gradeService.hasPassedAllPrerequisites(1L, 102L);

        assertFalse(result);
        List<CourseSummaryDto> missing = gradeService.getMissingPrerequisites(1L, 102L);
        assertEquals(1, missing.size());
        assertEquals("CS101", missing.get(0).getCode());
    }

    @Test
    @DisplayName("BR-04: Môn học có 2 điều kiện tiên quyết, sinh viên chỉ đạt 1 -> trả về false kèm môn còn thiếu")
    void shouldReturnFalseWhenOnlyPartialPrerequisitesMet() {
        PrerequisiteCondition cond1 = PrerequisiteCondition.builder().targetCourse(course201).prerequisiteCourse(course101).build();
        PrerequisiteCondition cond2 = PrerequisiteCondition.builder().targetCourse(course201).prerequisiteCourse(course102).build();

        when(prerequisiteRepository.findByTargetCourseId(201L)).thenReturn(List.of(cond1, cond2));
        when(gradeRecordRepository.existsByStudentIdAndCourseIdAndStatus(1L, 101L, GradeStatus.PASSED)).thenReturn(true);
        when(gradeRecordRepository.existsByStudentIdAndCourseIdAndStatus(1L, 102L, GradeStatus.PASSED)).thenReturn(false);

        boolean result = gradeService.hasPassedAllPrerequisites(1L, 201L);

        assertFalse(result);
        List<CourseSummaryDto> missing = gradeService.getMissingPrerequisites(1L, 201L);
        assertEquals(1, missing.size());
        assertEquals("CS102", missing.get(0).getCode());
    }
}
