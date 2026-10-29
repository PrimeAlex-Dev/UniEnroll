package com.unienroll.modules.engine.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.engine.dto.RegistrationPhaseRequest;
import com.unienroll.modules.engine.dto.RegistrationPhaseResponse;
import com.unienroll.modules.engine.entity.RegistrationPhase;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationPhaseService {

    private final RegistrationPhaseRepository phaseRepository;
    private final SemesterRepository semesterRepository;

    @Transactional
    public RegistrationPhaseResponse createPhase(RegistrationPhaseRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Thời gian bắt đầu đợt đăng ký phải trước thời gian kết thúc");
        }

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + request.getSemesterId()));

        RegistrationPhase phase = RegistrationPhase.builder()
                .semester(semester)
                .name(request.getName().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .targetCohort(StringUtils.hasText(request.getTargetCohort()) ? request.getTargetCohort().trim() : null)
                .build();

        RegistrationPhase saved = phaseRepository.save(phase);
        log.info("Created registration phase: id={}, name={}, semester={}", saved.getId(), saved.getName(), semester.getCode());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RegistrationPhaseResponse> getPhasesBySemester(Long semesterId) {
        return phaseRepository.findBySemesterId(semesterId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationPhaseResponse> getAllPhases() {
        return phaseRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * BR-01: Kiểm tra xem thời điểm giao dịch có nằm trong đợt đăng ký hợp lệ của sinh viên hay không
     */
    @Transactional(readOnly = true)
    public boolean isWithinActivePhase(Long semesterId, String cohort, Instant transTime) {
        List<RegistrationPhase> activePhases = phaseRepository.findActivePhases(semesterId, transTime);
        if (activePhases.isEmpty()) {
            return false;
        }

        for (RegistrationPhase phase : activePhases) {
            // Nếu đợt đăng ký không giới hạn cohort -> áp dụng cho tất cả
            if (!StringUtils.hasText(phase.getTargetCohort())) {
                return true;
            }
            // Nếu có giới hạn cohort -> phải trùng với cohort của sinh viên
            if (phase.getTargetCohort().equalsIgnoreCase(cohort)) {
                return true;
            }
        }
        return false;
    }

    private RegistrationPhaseResponse mapToResponse(RegistrationPhase phase) {
        Instant now = Instant.now();
        boolean isActive = now.isAfter(phase.getStartTime()) && now.isBefore(phase.getEndTime());

        return RegistrationPhaseResponse.builder()
                .id(phase.getId())
                .semesterId(phase.getSemester().getId())
                .semesterName(phase.getSemester().getName())
                .name(phase.getName())
                .startTime(phase.getStartTime())
                .endTime(phase.getEndTime())
                .targetCohort(phase.getTargetCohort())
                .active(isActive)
                .build();
    }
}
