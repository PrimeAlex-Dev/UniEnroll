package com.unienroll.modules.scheduling.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.scheduling.dto.SemesterRequest;
import com.unienroll.modules.scheduling.dto.SemesterResponse;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final CourseSectionRepository sectionRepository;
    private final RegistrationPhaseRepository phaseRepository;

    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Ngày bắt đầu học kỳ phải trước ngày kết thúc");
        }

        String code = request.getCode().toUpperCase().trim();
        if (semesterRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Mã học kỳ đã tồn tại: " + code);
        }

        Semester semester = Semester.builder()
                .code(code)
                .name(request.getName().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : SemesterStatus.PLANNED)
                .build();

        Semester saved = semesterRepository.save(semester);
        log.info("Created semester: id={}, code={}", saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemesterById(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + id));
        return mapToResponse(semester);
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getActiveSemesters() {
        return semesterRepository.findByStatus(SemesterStatus.ACTIVE).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public SemesterResponse updateSemester(Long id, SemesterRequest request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + id));

        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Ngày bắt đầu học kỳ phải trước ngày kết thúc");
        }

        String newCode = request.getCode().toUpperCase().trim();
        if (!semester.getCode().equalsIgnoreCase(newCode) && semesterRepository.existsByCode(newCode)) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Mã học kỳ đã tồn tại: " + newCode);
        }

        semester.setCode(newCode);
        semester.setName(request.getName().trim());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            semester.setStatus(request.getStatus());
        }

        Semester updated = semesterRepository.save(semester);
        log.info("Updated semester id={}, code={}", id, updated.getCode());
        return mapToResponse(updated);
    }

    @Transactional
    public SemesterResponse updateSemesterStatus(Long id, SemesterStatus status) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + id));

        semester.setStatus(status);
        Semester updated = semesterRepository.save(semester);
        log.info("Updated semester id={} status to {}", id, status);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteSemester(Long id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + id));

        if (sectionRepository.existsBySemesterId(id)) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Không thể xóa học kỳ đã có lớp học phần mở");
        }

        if (phaseRepository.existsBySemesterId(id)) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Không thể xóa học kỳ đã cấu hình đợt đăng ký");
        }

        semesterRepository.delete(semester);
        log.info("Deleted semester id={}, code={}", id, semester.getCode());
    }

    private SemesterResponse mapToResponse(Semester semester) {
        return SemesterResponse.builder()
                .id(semester.getId())
                .code(semester.getCode())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .status(semester.getStatus())
                .build();
    }
}
