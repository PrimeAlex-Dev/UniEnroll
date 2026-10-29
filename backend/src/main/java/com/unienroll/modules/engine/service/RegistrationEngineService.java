package com.unienroll.modules.engine.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.curriculum.dto.CourseSummaryDto;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.engine.dto.ActiveRegistrationsResponse;
import com.unienroll.modules.engine.dto.EnrollmentResponse;
import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.AuditExecutionStatus;
import com.unienroll.modules.reporting.event.RegistrationAuditEvent;
import com.unienroll.modules.reporting.service.GradeService;
import com.unienroll.modules.scheduling.dto.CourseSectionResponse;
import com.unienroll.modules.scheduling.dto.ScheduleSlotResponse;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.entity.ScheduleSlot;
import com.unienroll.modules.scheduling.entity.SectionStatus;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationEngineService {

    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRecordRepository enrollmentRepository;
    private final StudentProfileRepository studentRepository;
    private final RegistrationPhaseService phaseService;
    private final GradeService gradeService;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    /**
     * UC-14: ĐĂNG KÝ LỚP HỌC PHẦN (CORE TRANSACTION PIPELINE)
     * Thẩm tra tuần tự 6 Quy tắc Nghiệp vụ (BR-01 -> BR-06) và thực thi Khóa bi quan chống Race Condition.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentResponse enroll(Long studentId, Long sectionId, String ipAddress) {
        Instant transTime = Instant.now();

        // Nạp thông tin Sinh viên
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy hồ sơ sinh viên với ID: " + studentId));

        // Nạp thông tin Lớp học phần mục tiêu (chế độ đọc ban đầu)
        CourseSection targetSection = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        Long semesterId = targetSection.getSemester().getId();
        Course course = targetSection.getCourse();

        // -------------------------------------------------------------
        // BƯỚC 1: Validate BR-01 (Temporal Window Invariant)
        // -------------------------------------------------------------
        boolean inPhase = phaseService.isWithinActivePhase(semesterId, student.getCohort(), transTime);
        if (!inPhase) {
            publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                    ipAddress, AuditExecutionStatus.FAILED, "Cổng đăng ký học phần hiện đang đóng");
            throw new BusinessException(ErrorCode.ERR_REGISTRATION_WINDOW_CLOSED);
        }

        // -------------------------------------------------------------
        // BƯỚC 2: Validate BR-04 (Prerequisite Invariant)
        // -------------------------------------------------------------
        boolean passedPrereqs = gradeService.hasPassedAllPrerequisites(studentId, course.getId());
        if (!passedPrereqs) {
            List<CourseSummaryDto> missing = gradeService.getMissingPrerequisites(studentId, course.getId());
            publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                    ipAddress, AuditExecutionStatus.FAILED, "Chưa hoàn thành môn tiên quyết");
            throw new BusinessException(
                    ErrorCode.ERR_PREREQUISITE_NOT_FULFILLED,
                    "Sinh viên chưa đạt điều kiện tiên quyết cho môn: " + course.getName(),
                    Map.of("targetCourseCode", course.getCode(), "missingPrerequisites", missing)
            );
        }

        // -------------------------------------------------------------
        // BƯỚC 3: Validate BR-06 (Idempotency Invariant / No Duplicate Course)
        // -------------------------------------------------------------
        boolean alreadyEnrolledInCourse = enrollmentRepository.existsByStudentIdAndCourseIdAndSemesterIdAndStatus(
                studentId, course.getId(), semesterId, EnrollmentStatus.ENROLLED);
        if (alreadyEnrolledInCourse) {
            publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                    ipAddress, AuditExecutionStatus.FAILED, "Trùng môn học trong học kỳ");
            throw new BusinessException(
                    ErrorCode.ERR_DUPLICATE_COURSE_REGISTRATION,
                    "Bạn đã đăng ký một lớp học phần khác của môn học này trong kỳ: " + course.getCode()
            );
        }

        // Nạp các lớp sinh viên đang học hợp lệ trong kỳ để kiểm tra Trùng lịch (BR-03) và Hạn mức tín chỉ (BR-05)
        List<EnrollmentRecord> activeRecords = enrollmentRepository.findByStudentIdAndSemesterIdAndStatus(
                studentId, semesterId, EnrollmentStatus.ENROLLED);

        // -------------------------------------------------------------
        // BƯỚC 4: Validate BR-03 (Schedule Conflict Invariant)
        // -------------------------------------------------------------
        for (EnrollmentRecord activeRecord : activeRecords) {
            CourseSection enrolledSection = activeRecord.getSection();
            for (ScheduleSlot targetSlot : targetSection.getScheduleSlots()) {
                for (ScheduleSlot enrolledSlot : enrolledSection.getScheduleSlots()) {
                    if (targetSlot.getDayOfWeek() == enrolledSlot.getDayOfWeek()) {
                        if (isTimeOverlap(targetSlot.getStartPeriod(), targetSlot.getEndPeriod(),
                                enrolledSlot.getStartPeriod(), enrolledSlot.getEndPeriod())) {

                            String collisionMsg = String.format(
                                    "Lịch học của lớp đăng ký xung đột với lớp '%s' vào %s (Tiết %d-%d)",
                                    enrolledSection.getSectionCode(), targetSlot.getDayOfWeek(),
                                    targetSlot.getStartPeriod(), targetSlot.getEndPeriod()
                            );
                            publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                                    ipAddress, AuditExecutionStatus.FAILED, collisionMsg);

                            throw new BusinessException(
                                    ErrorCode.ERR_TIMETABLE_COLLISION,
                                    collisionMsg,
                                    Map.of(
                                            "conflictedSectionId", enrolledSection.getId(),
                                            "conflictedSectionCode", enrolledSection.getSectionCode(),
                                            "dayOfWeek", targetSlot.getDayOfWeek()
                                    )
                            );
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // BƯỚC 5: Validate BR-05 (Credit Limit Invariant - Max 24)
        // -------------------------------------------------------------
        int currentCredits = activeRecords.stream()
                .mapToInt(r -> r.getCourse().getTotalCredits())
                .sum();
        int resultingCredits = currentCredits + course.getTotalCredits();

        if (resultingCredits > 24 && !Boolean.TRUE.equals(student.getIsCreditLimitOverridden())) {
            String creditMsg = String.format(
                    "Không thể đăng ký. Tổng số tín chỉ (%d) vượt quá hạn mức tối đa cho phép trong học kỳ (24)",
                    resultingCredits
            );
            publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                    ipAddress, AuditExecutionStatus.FAILED, creditMsg);

            throw new BusinessException(
                    ErrorCode.ERR_CREDIT_LIMIT_EXCEEDED,
                    creditMsg,
                    Map.of(
                            "currentCredits", currentCredits,
                            "additionalCredits", course.getTotalCredits(),
                            "resultingCredits", resultingCredits,
                            "maxAllowedCredits", 24
                    )
            );
        }

        // -------------------------------------------------------------
        // BƯỚC 6: Validate BR-02 & Khóa dòng bi quan (PESSIMISTIC WRITE LOCK)
        // SELECT * FROM course_sections WHERE id = ? FOR UPDATE
        // -------------------------------------------------------------
        entityManager.refresh(targetSection, LockModeType.PESSIMISTIC_WRITE);
        CourseSection lockedSection = targetSection;

        if (lockedSection.getEnrolledCount() >= lockedSection.getCapacity()) {
            if (lockedSection.getStatus() != SectionStatus.CLOSED) {
                lockedSection.setStatus(SectionStatus.CLOSED);
                sectionRepository.save(lockedSection);
            }
            publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                    ipAddress, AuditExecutionStatus.FAILED, "Lớp học phần đã hết chỗ khả dụng");

            throw new BusinessException(
                    ErrorCode.ERR_SECTION_CAPACITY_FULL,
                    "Lớp học phần đã hết chỗ khả dụng",
                    Map.of(
                            "sectionId", sectionId,
                            "currentEnrolled", lockedSection.getEnrolledCount(),
                            "maxCapacity", lockedSection.getCapacity()
                    )
            );
        }

        // -------------------------------------------------------------
        // THỰC THI GIAO DỊCH NGUYÊN TỬ (ATOMIC TRANSACTION)
        // -------------------------------------------------------------
        lockedSection.setEnrolledCount(lockedSection.getEnrolledCount() + 1);
        if (lockedSection.getEnrolledCount().equals(lockedSection.getCapacity())) {
            lockedSection.setStatus(SectionStatus.CLOSED);
        }
        sectionRepository.save(lockedSection);

        // Lưu bản ghi EnrollmentRecord
        Optional<EnrollmentRecord> previousEnrollment = enrollmentRepository.findByStudentIdAndSectionId(studentId, sectionId);
        EnrollmentRecord record;

        if (previousEnrollment.isPresent()) {
            record = previousEnrollment.get();
            record.setStatus(EnrollmentStatus.ENROLLED);
            record.setEnrolledAt(transTime);
            record.setDroppedAt(null);
        } else {
            record = EnrollmentRecord.builder()
                    .student(student)
                    .section(lockedSection)
                    .course(course)
                    .semester(lockedSection.getSemester())
                    .status(EnrollmentStatus.ENROLLED)
                    .enrolledAt(transTime)
                    .build();
        }

        EnrollmentRecord savedRecord = enrollmentRepository.save(record);
        log.info("Successfully enrolled student {} into section {} (enrolledCount: {}/{})",
                student.getStudentCode(), lockedSection.getSectionCode(),
                lockedSection.getEnrolledCount(), lockedSection.getCapacity());

        // Phát sự kiện ghi log kiểm toán bất đồng bộ
        publishAuditLog(student, sectionId, course.getCode(), AuditActionType.ENROLL,
                ipAddress, AuditExecutionStatus.SUCCESS, null);

        return mapToEnrollmentResponse(savedRecord);
    }

    /**
     * UC-15: HỦY ĐĂNG KÝ LỚP HỌC PHẦN (DROP SECTION)
     * Thu hồi slot, mở lại trạng thái OPEN nếu đang CLOSED, kiểm tra ngưỡng tối thiểu 12 tín chỉ.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentResponse drop(Long studentId, Long sectionId, String ipAddress) {
        Instant transTime = Instant.now();

        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy hồ sơ sinh viên với ID: " + studentId));

        EnrollmentRecord record = enrollmentRepository.findByStudentIdAndSectionIdAndStatus(studentId, sectionId, EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_ENROLLMENT_NOT_FOUND, "Sinh viên chưa đăng ký lớp học phần này"));

        Long semesterId = record.getSemester().getId();

        // 1. Kiểm tra BR-01: Khung giờ hủy môn
        boolean inPhase = phaseService.isWithinActivePhase(semesterId, student.getCohort(), transTime);
        if (!inPhase) {
            publishAuditLog(student, sectionId, record.getCourse().getCode(), AuditActionType.DROP,
                    ipAddress, AuditExecutionStatus.FAILED, "Cổng đăng ký/hủy học phần hiện đang đóng");
            throw new BusinessException(ErrorCode.ERR_REGISTRATION_WINDOW_CLOSED);
        }

        // 2. Kiểm tra BR-05: Ngưỡng tối thiểu tín chỉ (Tối thiểu 12 tín chỉ sau khi rút)
        List<EnrollmentRecord> activeRecords = enrollmentRepository.findByStudentIdAndSemesterIdAndStatus(
                studentId, semesterId, EnrollmentStatus.ENROLLED);

        int currentCredits = activeRecords.stream()
                .mapToInt(r -> r.getCourse().getTotalCredits())
                .sum();
        int resultingCredits = currentCredits - record.getCourse().getTotalCredits();

        if (resultingCredits < 12 && !Boolean.TRUE.equals(student.getIsCreditLimitOverridden())) {
            String minCreditMsg = String.format(
                    "Không thể hủy lớp học phần này. Tổng số tín chỉ sau khi hủy (%d) thấp hơn hạn mức tối thiểu yêu cầu (12)",
                    resultingCredits
            );
            publishAuditLog(student, sectionId, record.getCourse().getCode(), AuditActionType.DROP,
                    ipAddress, AuditExecutionStatus.FAILED, minCreditMsg);

            throw new BusinessException(
                    ErrorCode.ERR_MINIMUM_CREDIT_UNMET,
                    minCreditMsg,
                    Map.of(
                            "currentCredits", currentCredits,
                            "droppedCredits", record.getCourse().getTotalCredits(),
                            "resultingCredits", resultingCredits,
                            "minAllowedCredits", 12
                    )
            );
        }

        // 3. Khóa dòng lớp học phần và hoàn trả slot trống
        CourseSection lockedSection = sectionRepository.findByIdWithPessimisticLock(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần để khóa: " + sectionId));

        lockedSection.setEnrolledCount(Math.max(0, lockedSection.getEnrolledCount() - 1));
        if (lockedSection.getStatus() == SectionStatus.CLOSED) {
            lockedSection.setStatus(SectionStatus.OPEN);
        }
        sectionRepository.save(lockedSection);

        // 4. Cập nhật bản ghi đăng ký thành DROPPED
        record.setStatus(EnrollmentStatus.DROPPED);
        record.setDroppedAt(transTime);
        EnrollmentRecord updatedRecord = enrollmentRepository.save(record);

        log.info("Successfully dropped student {} from section {} (remaining slots: {}/{})",
                student.getStudentCode(), lockedSection.getSectionCode(),
                lockedSection.getEnrolledCount(), lockedSection.getCapacity());

        // Phát sự kiện kiểm toán
        publishAuditLog(student, sectionId, record.getCourse().getCode(), AuditActionType.DROP,
                ipAddress, AuditExecutionStatus.SUCCESS, null);

        return mapToEnrollmentResponse(updatedRecord);
    }

    /**
     * UC-16: Xem Thời khóa biểu & Danh sách môn đã đăng ký
     */
    @Transactional(readOnly = true)
    public ActiveRegistrationsResponse getActiveRegistrations(Long studentId, Long semesterId) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy hồ sơ sinh viên với ID: " + studentId));

        List<EnrollmentRecord> records = enrollmentRepository.findByStudentIdAndSemesterIdAndStatus(
                studentId, semesterId, EnrollmentStatus.ENROLLED);

        int totalCredits = records.stream()
                .mapToInt(r -> r.getCourse().getTotalCredits())
                .sum();

        List<CourseSectionResponse> sectionResponses = records.stream()
                .map(r -> mapToCourseSectionResponse(r.getSection()))
                .toList();

        String semesterName = records.isEmpty() ? "" : records.get(0).getSemester().getName();

        return ActiveRegistrationsResponse.builder()
                .studentId(student.getId())
                .studentCode(student.getStudentCode())
                .studentName(student.getFullName())
                .semesterId(semesterId)
                .semesterName(semesterName)
                .totalCredits(totalCredits)
                .enrolledSections(sectionResponses)
                .build();
    }

    private boolean isTimeOverlap(int s1, int e1, int s2, int e2) {
        return Math.max(s1, s2) <= Math.min(e1, e2);
    }

    private void publishAuditLog(StudentProfile student, Long sectionId, String courseCode,
                                 AuditActionType actionType, String ipAddress,
                                 AuditExecutionStatus status, String errorMessage) {
        RegistrationAuditEvent event = RegistrationAuditEvent.builder()
                .actorId(student.getUser().getId())
                .actorUsername(student.getUser().getUsername())
                .actionType(actionType)
                .targetSectionId(sectionId)
                .targetCourseCode(courseCode)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .executionStatus(status)
                .errorMessage(errorMessage)
                .build();
        eventPublisher.publishEvent(event);
    }

    private EnrollmentResponse mapToEnrollmentResponse(EnrollmentRecord record) {
        return EnrollmentResponse.builder()
                .enrollmentId(record.getId())
                .studentId(record.getStudent().getId())
                .studentCode(record.getStudent().getStudentCode())
                .sectionId(record.getSection().getId())
                .sectionCode(record.getSection().getSectionCode())
                .courseCode(record.getCourse().getCode())
                .courseName(record.getCourse().getName())
                .totalCredits(record.getCourse().getTotalCredits())
                .status(record.getStatus())
                .enrolledAt(record.getEnrolledAt())
                .droppedAt(record.getDroppedAt())
                .build();
    }

    private CourseSectionResponse mapToCourseSectionResponse(CourseSection section) {
        List<ScheduleSlotResponse> slots = section.getScheduleSlots() != null
                ? section.getScheduleSlots().stream().map(s -> ScheduleSlotResponse.builder()
                .id(s.getId())
                .roomId(s.getRoom() != null ? s.getRoom().getId() : null)
                .roomNumber(s.getRoom() != null ? s.getRoom().getRoomNumber() : null)
                .building(s.getRoom() != null ? s.getRoom().getBuilding() : null)
                .roomCapacity(s.getRoom() != null ? s.getRoom().getCapacity() : null)
                .dayOfWeek(s.getDayOfWeek())
                .startPeriod(s.getStartPeriod())
                .endPeriod(s.getEndPeriod())
                .build()).toList()
                : List.of();

        return CourseSectionResponse.builder()
                .id(section.getId())
                .sectionCode(section.getSectionCode())
                .courseId(section.getCourse().getId())
                .courseCode(section.getCourse().getCode())
                .courseName(section.getCourse().getName())
                .totalCredits(section.getCourse().getTotalCredits())
                .semesterId(section.getSemester().getId())
                .semesterName(section.getSemester().getName())
                .lecturerId(section.getLecturer() != null ? section.getLecturer().getId() : null)
                .lecturerName(section.getLecturer() != null ? section.getLecturer().getFullName() : null)
                .capacity(section.getCapacity())
                .enrolledCount(section.getEnrolledCount())
                .availableSlots(Math.max(0, section.getCapacity() - section.getEnrolledCount()))
                .status(section.getStatus())
                .scheduleSlots(slots)
                .build();
    }
}
