package com.unienroll.modules.curriculum.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.curriculum.dto.CourseRequest;
import com.unienroll.modules.curriculum.dto.CourseResponse;
import com.unienroll.modules.curriculum.dto.CourseSummaryDto;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final PrerequisiteService prerequisiteService;

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        String code = request.getCode().toUpperCase().trim();
        if (courseRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.ERR_COURSE_CODE_DUPLICATE, "Mã môn học đã tồn tại: " + code);
        }

        int totalCredits = request.getTheoryCredits() + request.getPracticalCredits();
        if (totalCredits <= 0) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Tổng số tín chỉ (Lý thuyết + Thực hành) phải lớn hơn 0");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khoa với ID: " + request.getDepartmentId()));

        Course course = Course.builder()
                .code(code)
                .name(request.getName().trim())
                .theoryCredits(request.getTheoryCredits())
                .practicalCredits(request.getPracticalCredits())
                .totalCredits(totalCredits)
                .department(department)
                .description(request.getDescription())
                .build();

        Course saved = courseRepository.save(course);
        log.info("Created course: id={}, code={}, credits={}", saved.getId(), saved.getCode(), saved.getTotalCredits());
        return mapToResponse(saved);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học với ID: " + id));

        String newCode = request.getCode().toUpperCase().trim();
        if (!course.getCode().equalsIgnoreCase(newCode) && courseRepository.existsByCode(newCode)) {
            throw new BusinessException(ErrorCode.ERR_COURSE_CODE_DUPLICATE, "Mã môn học đã tồn tại: " + newCode);
        }

        int totalCredits = request.getTheoryCredits() + request.getPracticalCredits();
        if (totalCredits <= 0) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Tổng số tín chỉ phải lớn hơn 0");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy khoa với ID: " + request.getDepartmentId()));

        course.setCode(newCode);
        course.setName(request.getName().trim());
        course.setTheoryCredits(request.getTheoryCredits());
        course.setPracticalCredits(request.getPracticalCredits());
        course.setTotalCredits(totalCredits);
        course.setDepartment(department);
        course.setDescription(request.getDescription());

        Course updated = courseRepository.save(course);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học với ID: " + id));
        return mapToResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CourseResponse mapToResponse(Course course) {
        List<CourseSummaryDto> prereqs = prerequisiteService.getPrerequisites(course.getId());
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .theoryCredits(course.getTheoryCredits())
                .practicalCredits(course.getPracticalCredits())
                .totalCredits(course.getTotalCredits())
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .departmentName(course.getDepartment() != null ? course.getDepartment().getName() : null)
                .description(course.getDescription())
                .prerequisiteCourses(prereqs)
                .build();
    }
}
