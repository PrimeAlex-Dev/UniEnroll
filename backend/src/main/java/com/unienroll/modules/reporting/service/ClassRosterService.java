package com.unienroll.modules.reporting.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.reporting.dto.ClassRosterResponse;
import com.unienroll.modules.reporting.dto.ClassRosterStudentDto;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassRosterService {

    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRecordRepository enrollmentRepository;
    private final UserRepository userRepository;

    /**
     * UC-17: LẤY DANH SÁCH SINH VIÊN LỚP HỌC PHẦN (CLASS ROSTER)
     * Thẩm định quyền: Giảng viên chỉ được xem danh sách của lớp mình phụ trách.
     */
    @Transactional(readOnly = true)
    public ClassRosterResponse getClassRoster(Long currentUserId, Long sectionId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_USER_NOT_FOUND));

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        // Kiểm tra thẩm quyền: Giảng viên chỉ được xem lớp được phân công
        if (currentUser.getRole() == Role.ROLE_LECTURER) {
            if (section.getLecturer() == null || !section.getLecturer().getUser().getId().equals(currentUserId)) {
                log.warn("Lecturer id={} attempted unauthorized access to roster of sectionId={}", currentUserId, sectionId);
                throw new BusinessException(
                        ErrorCode.ERR_ACCESS_DENIED_NOT_ASSIGNED_LECTURER,
                        "Bạn không phải là giảng viên phụ trách lớp học phần này"
                );
            }
        }

        List<EnrollmentRecord> enrollments = enrollmentRepository.findBySectionIdAndStatus(
                sectionId, EnrollmentStatus.ENROLLED);

        List<ClassRosterStudentDto> studentDtos = enrollments.stream()
                .map(e -> ClassRosterStudentDto.builder()
                        .studentId(e.getStudent().getId())
                        .studentCode(e.getStudent().getStudentCode())
                        .fullName(e.getStudent().getFullName())
                        .cohort(e.getStudent().getCohort())
                        .major(e.getStudent().getMajor())
                        .email(e.getStudent().getUser().getEmail())
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .toList();

        return ClassRosterResponse.builder()
                .sectionId(section.getId())
                .sectionCode(section.getSectionCode())
                .courseCode(section.getCourse().getCode())
                .courseName(section.getCourse().getName())
                .lecturerName(section.getLecturer() != null ? section.getLecturer().getFullName() : "Chưa phân công")
                .enrolledCount(section.getEnrolledCount())
                .capacity(section.getCapacity())
                .students(studentDtos)
                .build();
    }
}
