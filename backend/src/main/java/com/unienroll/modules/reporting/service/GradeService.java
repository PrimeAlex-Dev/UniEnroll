package com.unienroll.modules.reporting.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.StudentProfile;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.curriculum.dto.CourseSummaryDto;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import com.unienroll.modules.reporting.dto.GradeRecordRequest;
import com.unienroll.modules.reporting.dto.GradeRecordResponse;
import com.unienroll.modules.reporting.entity.GradeRecord;
import com.unienroll.modules.reporting.entity.GradeStatus;
import com.unienroll.modules.reporting.repository.GradeRecordRepository;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRecordRepository gradeRecordRepository;
    private final StudentProfileRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final PrerequisiteConditionRepository prerequisiteRepository;

    /**
     * BR-04: Thẩm định xem sinh viên đã hoàn thành và đạt (PASSED) tất cả môn tiên quyết hay chưa
     */
    @Transactional(readOnly = true)
    public boolean hasPassedAllPrerequisites(Long studentId, Long courseId) {
        List<CourseSummaryDto> missing = getMissingPrerequisites(studentId, courseId);
        return missing.isEmpty();
    }

    /**
     * BR-04: Trả về danh sách các môn tiên quyết mà sinh viên CHƯA ĐẠT (phục vụ sinh thông báo lỗi chi tiết)
     */
    @Transactional(readOnly = true)
    public List<CourseSummaryDto> getMissingPrerequisites(Long studentId, Long courseId) {
        List<PrerequisiteCondition> conditions = prerequisiteRepository.findByTargetCourseId(courseId);
        if (conditions.isEmpty()) {
            return List.of();
        }

        List<CourseSummaryDto> missingList = new ArrayList<>();
        for (PrerequisiteCondition condition : conditions) {
            Course prereq = condition.getPrerequisiteCourse();
            boolean passed = gradeRecordRepository.existsByStudentIdAndCourseIdAndStatus(
                    studentId, prereq.getId(), GradeStatus.PASSED);

            if (!passed) {
                missingList.add(CourseSummaryDto.builder()
                        .id(prereq.getId())
                        .code(prereq.getCode())
                        .name(prereq.getName())
                        .totalCredits(prereq.getTotalCredits())
                        .build());
            }
        }
        return missingList;
    }

    /**
     * Nhập hoặc cập nhật điểm tích lũy của sinh viên
     */
    @Transactional
    public GradeRecordResponse recordGrade(GradeRecordRequest request) {
        StudentProfile student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy sinh viên với ID: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học với ID: " + request.getCourseId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + request.getSemesterId()));

        Optional<GradeRecord> existing = gradeRecordRepository.findByStudentIdAndCourseId(student.getId(), course.getId());
        GradeRecord record;

        if (existing.isPresent()) {
            record = existing.get();
            record.setSemester(semester);
            record.setFinalScore(request.getFinalScore());
            record.setStatus(request.getStatus());
        } else {
            record = GradeRecord.builder()
                    .student(student)
                    .course(course)
                    .semester(semester)
                    .finalScore(request.getFinalScore())
                    .status(request.getStatus())
                    .build();
        }

        GradeRecord saved = gradeRecordRepository.save(record);
        log.info("Recorded grade: student={}, course={}, status={}, score={}",
                student.getStudentCode(), course.getCode(), saved.getStatus(), saved.getFinalScore());

        return mapToResponse(saved);
    }

    /**
     * Truy xuất toàn bộ bảng điểm tích lũy của sinh viên
     */
    @Transactional(readOnly = true)
    public List<GradeRecordResponse> getStudentTranscript(Long studentId) {
        return gradeRecordRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private GradeRecordResponse mapToResponse(GradeRecord record) {
        return GradeRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent().getId())
                .studentCode(record.getStudent().getStudentCode())
                .studentName(record.getStudent().getFullName())
                .courseId(record.getCourse().getId())
                .courseCode(record.getCourse().getCode())
                .courseName(record.getCourse().getName())
                .totalCredits(record.getCourse().getTotalCredits())
                .semesterId(record.getSemester().getId())
                .semesterName(record.getSemester().getName())
                .finalScore(record.getFinalScore())
                .status(record.getStatus())
                .build();
    }
}
