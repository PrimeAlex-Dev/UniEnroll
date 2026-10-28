package com.unienroll.modules.curriculum.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.curriculum.dto.CourseSummaryDto;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrerequisiteService {

    private final CourseRepository courseRepository;
    private final PrerequisiteConditionRepository prerequisiteRepository;

    @Transactional
    public List<CourseSummaryDto> assignPrerequisites(Long targetCourseId, List<Long> prerequisiteCourseIds) {
        Course targetCourse = courseRepository.findById(targetCourseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học với ID: " + targetCourseId));

        for (Long prereqId : prerequisiteCourseIds) {
            // Ràng buộc 1: Không thể tự đặt chính mình làm môn tiên quyết
            if (targetCourseId.equals(prereqId)) {
                throw new BusinessException(
                        ErrorCode.ERR_CIRCULAR_PREREQUISITE_DETECTED,
                        "Môn học không thể tự làm điều kiện tiên quyết cho chính mình (" + targetCourse.getCode() + ")"
                );
            }

            Course prereqCourse = courseRepository.findById(prereqId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học tiên quyết với ID: " + prereqId));

            // Ràng buộc 2: Kiểm tra chu trình phụ thuộc vòng tròn (Circular Dependency)
            if (isReachable(prereqId, targetCourseId)) {
                throw new BusinessException(
                        ErrorCode.ERR_CIRCULAR_PREREQUISITE_DETECTED,
                        String.format("Phát hiện phụ thuộc vòng: Môn '%s' đã gián tiếp phụ thuộc vào môn '%s'",
                                prereqCourse.getCode(), targetCourse.getCode())
                );
            }

            // Lưu quan hệ nếu chưa có
            if (!prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(targetCourseId, prereqId)) {
                PrerequisiteCondition condition = PrerequisiteCondition.builder()
                        .targetCourse(targetCourse)
                        .prerequisiteCourse(prereqCourse)
                        .build();
                prerequisiteRepository.save(condition);
                log.info("Assigned prerequisite: target='{}' requires prereq='{}'", targetCourse.getCode(), prereqCourse.getCode());
            }
        }

        return getPrerequisites(targetCourseId);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> getPrerequisites(Long targetCourseId) {
        if (!courseRepository.existsById(targetCourseId)) {
            throw new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học với ID: " + targetCourseId);
        }

        List<PrerequisiteCondition> conditions = prerequisiteRepository.findByTargetCourseId(targetCourseId);
        return conditions.stream()
                .map(c -> CourseSummaryDto.builder()
                        .id(c.getPrerequisiteCourse().getId())
                        .code(c.getPrerequisiteCourse().getCode())
                        .name(c.getPrerequisiteCourse().getName())
                        .totalCredits(c.getPrerequisiteCourse().getTotalCredits())
                        .build())
                .toList();
    }

    @Transactional
    public void removePrerequisite(Long targetCourseId, Long prerequisiteCourseId) {
        if (!prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(targetCourseId, prerequisiteCourseId)) {
            throw new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy quan hệ tiên quyết tương ứng");
        }
        prerequisiteRepository.deleteByTargetCourseIdAndPrerequisiteCourseId(targetCourseId, prerequisiteCourseId);
        log.info("Removed prerequisite: targetId={}, prereqId={}", targetCourseId, prerequisiteCourseId);
    }

    /**
     * Thuật toán DFS kiểm tra xem từ startCourseId có đường đi nào tới targetCourseId thông qua các điều kiện tiên quyết hay không.
     * Nếu có đường đi, nghĩa là startCourseId phụ thuộc vào targetCourseId -> việc thêm targetCourseId phụ thuộc startCourseId sẽ tạo chu trình!
     */
    public boolean isReachable(Long startCourseId, Long targetCourseId) {
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        queue.add(startCourseId);
        visited.add(startCourseId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(targetCourseId)) {
                return true;
            }

            List<PrerequisiteCondition> edges = prerequisiteRepository.findByTargetCourseId(current);
            for (PrerequisiteCondition edge : edges) {
                Long next = edge.getPrerequisiteCourse().getId();
                if (next.equals(targetCourseId)) {
                    return true;
                }
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return false;
    }
}
