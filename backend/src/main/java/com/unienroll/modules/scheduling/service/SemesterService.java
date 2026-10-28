package com.unienroll.modules.scheduling.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.scheduling.dto.SemesterRequest;
import com.unienroll.modules.scheduling.dto.SemesterResponse;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
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
