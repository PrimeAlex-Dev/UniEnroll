package com.unienroll.modules.curriculum.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrerequisiteCycleDetectionTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PrerequisiteConditionRepository prerequisiteRepository;

    @InjectMocks
    private PrerequisiteService prerequisiteService;

    private Course courseA;
    private Course courseB;
    private Course courseC;
    private Course courseD;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().id(1L).code("IT").name("CNTT").build();

        courseA = Course.builder().id(1L).code("CS101").name("Nhập môn Lập trình").totalCredits(3).department(dept).build();
        courseB = Course.builder().id(2L).code("CS102").name("Cấu trúc Dữ liệu").totalCredits(4).department(dept).build();
        courseC = Course.builder().id(3L).code("CS201").name("Thiết kế Thuật toán").totalCredits(4).department(dept).build();
        courseD = Course.builder().id(4L).code("CS301").name("Đồ án Phần mềm").totalCredits(3).department(dept).build();
    }

    @Test
    @DisplayName("Chặn vi phạm: Môn học không thể tự làm điều kiện tiên quyết cho chính mình (A -> A)")
    void shouldRejectSelfReferencingPrerequisite() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseA));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                prerequisiteService.assignPrerequisites(1L, List.of(1L))
        );

        assertEquals(ErrorCode.ERR_CIRCULAR_PREREQUISITE_DETECTED, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("tự làm điều kiện tiên quyết cho chính mình"));
    }

    @Test
    @DisplayName("Chặn chu trình 2 đỉnh: A -> B đã có, không cho phép gán B -> A")
    void shouldDetect2NodeCycle() {
        // Giả sử môn B đã phụ thuộc vào môn A (B requires A)
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseA));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(courseB));

        // Khi kiểm tra prereqId=2 có đường đi tới targetId=1 không:
        // edges từ B: B -> A
        PrerequisiteCondition edgeBtoA = PrerequisiteCondition.builder()
                .targetCourse(courseB)
                .prerequisiteCourse(courseA)
                .build();
        when(prerequisiteRepository.findByTargetCourseId(2L)).thenReturn(List.of(edgeBtoA));

        // Thử gán: A requires B -> phải bị chặn vì B đã phụ thuộc A
        BusinessException ex = assertThrows(BusinessException.class, () ->
                prerequisiteService.assignPrerequisites(1L, List.of(2L))
        );

        assertEquals(ErrorCode.ERR_CIRCULAR_PREREQUISITE_DETECTED, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Phát hiện phụ thuộc vòng"));
    }

    @Test
    @DisplayName("Chặn chu trình 3 đỉnh: A -> B -> C đã có, không cho phép gán C -> A")
    void shouldDetect3NodeCycle() {
        // Đã có: C requires B, B requires A
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseA));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(courseC));

        // C -> B
        PrerequisiteCondition edgeCtoB = PrerequisiteCondition.builder()
                .targetCourse(courseC)
                .prerequisiteCourse(courseB)
                .build();
        when(prerequisiteRepository.findByTargetCourseId(3L)).thenReturn(List.of(edgeCtoB));

        // B -> A
        PrerequisiteCondition edgeBtoA = PrerequisiteCondition.builder()
                .targetCourse(courseB)
                .prerequisiteCourse(courseA)
                .build();
        when(prerequisiteRepository.findByTargetCourseId(2L)).thenReturn(List.of(edgeBtoA));

        // Thử gán: A requires C -> tạo chu trình A -> C -> B -> A
        BusinessException ex = assertThrows(BusinessException.class, () ->
                prerequisiteService.assignPrerequisites(1L, List.of(3L))
        );

        assertEquals(ErrorCode.ERR_CIRCULAR_PREREQUISITE_DETECTED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Cho phép đồ thị không chu trình (DAG hợp lệ): B và C cùng phụ thuộc A")
    void shouldAllowValidDirectedAcyclicGraph() {
        when(courseRepository.findById(2L)).thenReturn(Optional.of(courseB));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseA));
        when(courseRepository.existsById(2L)).thenReturn(true);

        // Từ A chưa có edge nào tới B
        when(prerequisiteRepository.findByTargetCourseId(1L)).thenReturn(List.of());
        when(prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(2L, 1L)).thenReturn(false);

        List<?> result = prerequisiteService.assignPrerequisites(2L, List.of(1L));

        assertNotNull(result);
        verify(prerequisiteRepository, times(1)).save(any(PrerequisiteCondition.class));
    }
}
