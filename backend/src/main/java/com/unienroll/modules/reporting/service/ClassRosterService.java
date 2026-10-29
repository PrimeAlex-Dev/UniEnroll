package com.unienroll.modules.reporting.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.reporting.dto.ClassRosterResponse;
import com.unienroll.modules.reporting.dto.ClassRosterStudentDto;
import com.unienroll.modules.scheduling.dto.ScheduleSlotResponse;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.ScheduleSlotRepository;
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
    private final StudentProfileRepository studentProfileRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;

    /**
     * UC-17: LẤY DANH SÁCH SINH VIÊN LỚP HỌC PHẦN (CLASS ROSTER)
     * Thẩm định quyền:
     * - Giảng viên chỉ được xem danh sách của lớp mình phụ trách.
     * - Sinh viên chỉ được xem danh sách của lớp mà mình đang ghi danh học.
     * - Staff/Admin có toàn quyền tra cứu.
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

        // Kiểm tra thẩm quyền: Sinh viên chỉ được xem lớp mình đang theo học
        if (currentUser.getRole() == Role.ROLE_STUDENT) {
            StudentProfile student = studentProfileRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy hồ sơ sinh viên tương ứng"));
            boolean isEnrolled = enrollmentRepository.findByStudentIdAndSectionIdAndStatus(
                    student.getId(), sectionId, EnrollmentStatus.ENROLLED).isPresent();
            if (!isEnrolled) {
                log.warn("Student id={} attempted unauthorized access to roster of sectionId={}", currentUserId, sectionId);
                throw new BusinessException(
                        ErrorCode.ERR_FORBIDDEN,
                        "Bạn không tham gia lớp học phần này nên không thể xem thông tin lớp"
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

        List<ScheduleSlotResponse> slots = scheduleSlotRepository.findBySectionId(sectionId).stream()
                .map(s -> ScheduleSlotResponse.builder()
                        .id(s.getId())
                        .roomId(s.getRoom() != null ? s.getRoom().getId() : null)
                        .roomNumber(s.getRoom() != null ? s.getRoom().getRoomNumber() : null)
                        .building(s.getRoom() != null ? s.getRoom().getBuilding() : null)
                        .roomCapacity(s.getRoom() != null ? s.getRoom().getCapacity() : null)
                        .dayOfWeek(s.getDayOfWeek())
                        .startPeriod(s.getStartPeriod())
                        .endPeriod(s.getEndPeriod())
                        .build())
                .toList();

        return ClassRosterResponse.builder()
                .sectionId(section.getId())
                .sectionCode(section.getSectionCode())
                .courseCode(section.getCourse().getCode())
                .courseName(section.getCourse().getName())
                .lecturerName(section.getLecturer() != null ? section.getLecturer().getFullName() : "Chưa phân công")
                .lecturerEmail(section.getLecturer() != null && section.getLecturer().getUser() != null ? section.getLecturer().getUser().getEmail() : null)
                .enrolledCount(section.getEnrolledCount())
                .capacity(section.getCapacity())
                .schedules(slots)
                .students(studentDtos)
                .build();
    }
}
