package com.unienroll.modules.reporting.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.reporting.dto.OccupancyDashboardResponse;
import com.unienroll.modules.reporting.dto.SectionOccupancyDto;
import com.unienroll.modules.scheduling.entity.CourseSection;
import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OccupancyReportService {

    private final SemesterRepository semesterRepository;
    private final CourseSectionRepository sectionRepository;

    /**
     * UC-18: BÁO CÁO TỶ LỆ LẤP ĐẦY LỚP HỌC PHẦN (OCCUPANCY DASHBOARD)
     * Thống kê tổng hợp toàn trường và phân loại: Đầy 100%, Ổn định (50%-99%), Nguy cơ hủy (< 15 sinh viên).
     */
    @Transactional(readOnly = true)
    public OccupancyDashboardResponse getOccupancyDashboard(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + semesterId));

        List<CourseSection> sections = sectionRepository.findBySemesterId(semesterId);

        int totalSections = sections.size();
        int totalCapacity = sections.stream().mapToInt(CourseSection::getCapacity).sum();
        int totalEnrolled = sections.stream().mapToInt(CourseSection::getEnrolledCount).sum();

        double overallOccupancyRate = 0.0;
        if (totalCapacity > 0) {
            overallOccupancyRate = BigDecimal.valueOf((double) totalEnrolled / totalCapacity * 100)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        List<SectionOccupancyDto> fullSections = new ArrayList<>();
        List<SectionOccupancyDto> optimalSections = new ArrayList<>();
        List<SectionOccupancyDto> atRiskSections = new ArrayList<>();

        for (CourseSection section : sections) {
            double rate = 0.0;
            if (section.getCapacity() > 0) {
                rate = BigDecimal.valueOf((double) section.getEnrolledCount() / section.getCapacity() * 100)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            SectionOccupancyDto dto = SectionOccupancyDto.builder()
                    .sectionId(section.getId())
                    .sectionCode(section.getSectionCode())
                    .courseCode(section.getCourse().getCode())
                    .courseName(section.getCourse().getName())
                    .lecturerName(section.getLecturer() != null ? section.getLecturer().getFullName() : "Chưa phân công")
                    .enrolledCount(section.getEnrolledCount())
                    .capacity(section.getCapacity())
                    .occupancyRate(rate)
                    .status(section.getStatus())
                    .build();

            // Phân loại nhóm theo đặc tả UC-18
            if (section.getEnrolledCount() >= section.getCapacity()) {
                fullSections.add(dto);
            } else if (section.getEnrolledCount() < 15) {
                atRiskSections.add(dto);
            } else {
                optimalSections.add(dto);
            }
        }

        return OccupancyDashboardResponse.builder()
                .semesterId(semester.getId())
                .semesterName(semester.getName())
                .totalSections(totalSections)
                .totalCapacity(totalCapacity)
                .totalEnrolled(totalEnrolled)
                .overallOccupancyRate(overallOccupancyRate)
                .fullSections(fullSections)
                .optimalSections(optimalSections)
                .atRiskSections(atRiskSections)
                .build();
    }
}
