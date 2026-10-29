package com.unienroll.modules.scheduling.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.entity.LecturerProfile;
import com.unienroll.modules.auth.repository.LecturerProfileRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.engine.repository.EnrollmentRecordRepository;
import com.unienroll.modules.scheduling.dto.*;
import com.unienroll.modules.scheduling.entity.*;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.RoomRepository;
import com.unienroll.modules.scheduling.repository.ScheduleSlotRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSectionService {

    private final CourseSectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final LecturerProfileRepository lecturerRepository;
    private final RoomRepository roomRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;

    /**
     * UC-09: Mở Lớp Học phần Mới
     */
    @Transactional
    public CourseSectionResponse createSection(CourseSectionRequest request) {
        String sectionCode = request.getSectionCode().toUpperCase().trim();
        if (sectionRepository.existsBySemesterIdAndSectionCode(request.getSemesterId(), sectionCode)) {
            throw new BusinessException(ErrorCode.ERR_SECTION_CODE_DUPLICATE,
                    String.format("Mã lớp học phần '%s' đã tồn tại trong học kỳ này", sectionCode));
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy môn học với ID: " + request.getCourseId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy học kỳ với ID: " + request.getSemesterId()));

        LecturerProfile lecturer = null;
        if (request.getLecturerId() != null) {
            lecturer = lecturerRepository.findById(request.getLecturerId())
                    .or(() -> lecturerRepository.findByUserId(request.getLecturerId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy giảng viên với ID: " + request.getLecturerId()));
        }

        CourseSection section = CourseSection.builder()
                .sectionCode(sectionCode)
                .course(course)
                .semester(semester)
                .lecturer(lecturer)
                .capacity(request.getCapacity())
                .enrolledCount(0)
                .status(SectionStatus.OPEN)
                .build();

        CourseSection saved = sectionRepository.save(section);
        log.info("Created section: id={}, code={}, capacity={}", saved.getId(), saved.getSectionCode(), saved.getCapacity());
        return mapToResponse(saved);
    }

    /**
     * UC-10: Cấu hình Lịch học Chi tiết (Schedule Slots) & Thẩm định Xung đột
     */
    @Transactional
    public CourseSectionResponse assignScheduleSlots(Long sectionId, List<ScheduleSlotRequest> slotRequests) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        List<ScheduleSlot> newSlots = new ArrayList<>();

        for (ScheduleSlotRequest req : slotRequests) {
            if (req.getStartPeriod() > req.getEndPeriod()) {
                throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Tiết bắt đầu phải nhỏ hơn hoặc bằng tiết kết thúc");
            }

            Room room = roomRepository.findById(req.getRoomId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy phòng học với ID: " + req.getRoomId()));

            // Ràng buộc 1: Sức chứa phòng học phải >= sức chứa tối đa của lớp học phần
            if (room.getCapacity() < section.getCapacity()) {
                throw new BusinessException(ErrorCode.ERR_ROOM_CAPACITY_INSUFFICIENT,
                        String.format("Phòng %s (sức chứa %d) không đủ cho lớp học phần (chỉ tiêu %d)",
                                room.getRoomNumber(), room.getCapacity(), section.getCapacity()));
            }

            // Ràng buộc 2 (BR-03): Kiểm tra xung đột phòng học trong cùng học kỳ
            List<ScheduleSlot> roomSlots = scheduleSlotRepository.findExistingSlotsByRoom(
                    section.getSemester().getId(), room.getId(), req.getDayOfWeek());

            for (ScheduleSlot existing : roomSlots) {
                // Bỏ qua nếu là chính lớp này đang cấu hình
                if (existing.getSection().getId().equals(section.getId())) {
                    continue;
                }
                if (isTimeOverlap(req.getStartPeriod(), req.getEndPeriod(), existing.getStartPeriod(), existing.getEndPeriod())) {
                    throw new BusinessException(ErrorCode.ERR_ROOM_SCHEDULE_COLLISION,
                            String.format("Phòng %s bị trùng lịch với lớp '%s' vào %s (Tiết %d-%d)",
                                    room.getRoomNumber(), existing.getSection().getSectionCode(),
                                    req.getDayOfWeek(), existing.getStartPeriod(), existing.getEndPeriod()));
                }
            }

            // Ràng buộc 3 (BR-03): Kiểm tra xung đột lịch giảng viên trong cùng học kỳ (nếu đã phân công)
            if (section.getLecturer() != null) {
                List<ScheduleSlot> lecturerSlots = scheduleSlotRepository.findExistingSlotsByLecturer(
                        section.getSemester().getId(), section.getLecturer().getId(), req.getDayOfWeek());

                for (ScheduleSlot existing : lecturerSlots) {
                    if (existing.getSection().getId().equals(section.getId())) {
                        continue;
                    }
                    if (isTimeOverlap(req.getStartPeriod(), req.getEndPeriod(), existing.getStartPeriod(), existing.getEndPeriod())) {
                        throw new BusinessException(ErrorCode.ERR_LECTURER_SCHEDULE_COLLISION,
                                String.format("Giảng viên %s bị trùng lịch dạy với lớp '%s' vào %s (Tiết %d-%d)",
                                        section.getLecturer().getFullName(), existing.getSection().getSectionCode(),
                                        req.getDayOfWeek(), existing.getStartPeriod(), existing.getEndPeriod()));
                    }
                }
            }

            ScheduleSlot slot = ScheduleSlot.builder()
                    .section(section)
                    .room(room)
                    .dayOfWeek(req.getDayOfWeek())
                    .startPeriod(req.getStartPeriod())
                    .endPeriod(req.getEndPeriod())
                    .build();
            newSlots.add(slot);
        }

        // Cập nhật danh sách slots
        section.getScheduleSlots().clear();
        section.getScheduleSlots().addAll(newSlots);

        CourseSection updated = sectionRepository.save(section);
        log.info("Assigned {} schedule slots to section id={}", newSlots.size(), sectionId);
        return mapToResponse(updated);
    }

    /**
     * UC-10: Thêm 1 buổi học vào lịch hiện tại của lớp
     */
    @Transactional
    public CourseSectionResponse addSingleScheduleSlot(Long sectionId, ScheduleSlotRequest req) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        if (req.getStartPeriod() > req.getEndPeriod()) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED, "Tiết bắt đầu phải nhỏ hơn hoặc bằng tiết kết thúc");
        }

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy phòng học với ID: " + req.getRoomId()));

        if (room.getCapacity() < section.getCapacity()) {
            throw new BusinessException(ErrorCode.ERR_ROOM_CAPACITY_INSUFFICIENT,
                    String.format("Phòng %s (sức chứa %d) không đủ cho lớp học phần (chỉ tiêu %d)",
                            room.getRoomNumber(), room.getCapacity(), section.getCapacity()));
        }

        for (ScheduleSlot existing : section.getScheduleSlots()) {
            if (existing.getDayOfWeek().equals(req.getDayOfWeek())
                    && isTimeOverlap(req.getStartPeriod(), req.getEndPeriod(), existing.getStartPeriod(), existing.getEndPeriod())) {
                throw new BusinessException(ErrorCode.ERR_TIMETABLE_COLLISION,
                        String.format("Lớp này đã có lịch vào %s (Tiết %d-%d)",
                                req.getDayOfWeek(), existing.getStartPeriod(), existing.getEndPeriod()));
            }
        }

        List<ScheduleSlot> roomSlots = scheduleSlotRepository.findExistingSlotsByRoom(
                section.getSemester().getId(), room.getId(), req.getDayOfWeek());
        for (ScheduleSlot existing : roomSlots) {
            if (existing.getSection().getId().equals(section.getId())) {
                continue;
            }
            if (isTimeOverlap(req.getStartPeriod(), req.getEndPeriod(), existing.getStartPeriod(), existing.getEndPeriod())) {
                throw new BusinessException(ErrorCode.ERR_ROOM_SCHEDULE_COLLISION,
                        String.format("Phòng %s bị trùng lịch với lớp '%s' vào %s (Tiết %d-%d)",
                                room.getRoomNumber(), existing.getSection().getSectionCode(),
                                req.getDayOfWeek(), existing.getStartPeriod(), existing.getEndPeriod()));
            }
        }

        if (section.getLecturer() != null) {
            List<ScheduleSlot> lecturerSlots = scheduleSlotRepository.findExistingSlotsByLecturer(
                    section.getSemester().getId(), section.getLecturer().getId(), req.getDayOfWeek());
            for (ScheduleSlot existing : lecturerSlots) {
                if (existing.getSection().getId().equals(section.getId())) {
                    continue;
                }
                if (isTimeOverlap(req.getStartPeriod(), req.getEndPeriod(), existing.getStartPeriod(), existing.getEndPeriod())) {
                    throw new BusinessException(ErrorCode.ERR_LECTURER_SCHEDULE_COLLISION,
                            String.format("Giảng viên %s bị trùng lịch dạy với lớp '%s' vào %s (Tiết %d-%d)",
                                    section.getLecturer().getFullName(), existing.getSection().getSectionCode(),
                                    req.getDayOfWeek(), existing.getStartPeriod(), existing.getEndPeriod()));
                }
            }
        }

        ScheduleSlot slot = ScheduleSlot.builder()
                .section(section)
                .room(room)
                .dayOfWeek(req.getDayOfWeek())
                .startPeriod(req.getStartPeriod())
                .endPeriod(req.getEndPeriod())
                .build();
        section.getScheduleSlots().add(slot);

        CourseSection updated = sectionRepository.save(section);
        log.info("Added schedule slot to section id={}, day={}, periods={}-{}",
                sectionId, req.getDayOfWeek(), req.getStartPeriod(), req.getEndPeriod());
        return mapToResponse(updated);
    }

    /**
     * UC-10: Xóa 1 buổi học khỏi lớp học phần
     */
    @Transactional
    public CourseSectionResponse deleteScheduleSlot(Long sectionId, Long slotId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        boolean removed = section.getScheduleSlots().removeIf(slot -> slot.getId() != null && slot.getId().equals(slotId));
        if (!removed) {
            throw new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lịch học với ID: " + slotId);
        }

        CourseSection updated = sectionRepository.save(section);
        log.info("Deleted schedule slot id={} from section id={}", slotId, sectionId);
        return mapToResponse(updated);
    }

    /**
     * UC-11: Điều chỉnh Sức chứa Lớp học phần (BR-02 & BR-09)
     */
    @Transactional
    public CourseSectionResponse updateCapacity(Long sectionId, int newCapacity) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        // Ràng buộc BR-09: Không được hạ sức chứa thấp hơn số lượng đã đăng ký thành công
        if (newCapacity < section.getEnrolledCount()) {
            throw new BusinessException(ErrorCode.ERR_CAPACITY_LOWER_THAN_ENROLLED,
                    String.format("Không thể giảm sức chứa (%d) xuống thấp hơn số sinh viên hiện đã đăng ký (%d)",
                            newCapacity, section.getEnrolledCount()));
        }

        // Kiểm tra với sức chứa của tất cả phòng học đã phân bổ
        for (ScheduleSlot slot : section.getScheduleSlots()) {
            if (newCapacity > slot.getRoom().getCapacity()) {
                throw new BusinessException(ErrorCode.ERR_ROOM_CAPACITY_INSUFFICIENT,
                        String.format("Sức chứa mới (%d) vượt quá sức chứa tối đa của phòng %s (%d)",
                                newCapacity, slot.getRoom().getRoomNumber(), slot.getRoom().getCapacity()));
            }
        }

        section.setCapacity(newCapacity);

        // Tự động mở lại trạng thái OPEN nếu lớp từng bị đóng do đầy slot
        if (section.getEnrolledCount() < newCapacity && section.getStatus() == SectionStatus.CLOSED) {
            section.setStatus(SectionStatus.OPEN);
            log.info("Auto reopened section id={} as capacity increased from full", sectionId);
        }

        CourseSection updated = sectionRepository.save(section);
        log.info("Updated capacity for section id={}: newCapacity={}", sectionId, newCapacity);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public CourseSectionResponse getSectionById(Long id) {
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + id));
        return mapToResponse(section);
    }

    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getSectionsBySemester(Long semesterId) {
        return sectionRepository.findBySemesterId(semesterId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseSectionResponse> getAllSections() {
        return sectionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CourseSectionResponse cancelSection(Long sectionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));
        section.setStatus(SectionStatus.CANCELLED);
        CourseSection saved = sectionRepository.save(section);
        log.info("Cancelled section id={}", sectionId);
        return mapToResponse(saved);
    }

    /**
     * UC-09: Cập nhật thông tin Lớp học phần (Mã lớp, Giảng viên, Sức chứa)
     */
    @Transactional
    public CourseSectionResponse updateSection(Long sectionId, CourseSectionRequest request) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        String newCode = request.getSectionCode().toUpperCase().trim();
        if (!section.getSectionCode().equalsIgnoreCase(newCode)
                && sectionRepository.existsBySemesterIdAndSectionCode(section.getSemester().getId(), newCode)) {
            throw new BusinessException(ErrorCode.ERR_SECTION_CODE_DUPLICATE,
                    String.format("Mã lớp học phần '%s' đã tồn tại trong học kỳ này", newCode));
        }

        if (request.getLecturerId() != null) {
            LecturerProfile newLecturer = lecturerRepository.findById(request.getLecturerId())
                    .or(() -> lecturerRepository.findByUserId(request.getLecturerId()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy giảng viên với ID: " + request.getLecturerId()));

            for (ScheduleSlot slot : section.getScheduleSlots()) {
                List<ScheduleSlot> lecturerSlots = scheduleSlotRepository.findExistingSlotsByLecturer(
                        section.getSemester().getId(), newLecturer.getId(), slot.getDayOfWeek());
                for (ScheduleSlot existing : lecturerSlots) {
                    if (existing.getSection().getId().equals(section.getId())) {
                        continue;
                    }
                    if (isTimeOverlap(slot.getStartPeriod(), slot.getEndPeriod(), existing.getStartPeriod(), existing.getEndPeriod())) {
                        throw new BusinessException(ErrorCode.ERR_LECTURER_SCHEDULE_COLLISION,
                                String.format("Giảng viên %s bị trùng lịch dạy với lớp '%s' vào %s (Tiết %d-%d)",
                                        newLecturer.getFullName(), existing.getSection().getSectionCode(),
                                        slot.getDayOfWeek(), existing.getStartPeriod(), existing.getEndPeriod()));
                    }
                }
            }
            section.setLecturer(newLecturer);
        } else {
            section.setLecturer(null);
        }

        if (request.getCapacity() != null && request.getCapacity() > 0) {
            if (request.getCapacity() < section.getEnrolledCount()) {
                throw new BusinessException(ErrorCode.ERR_CAPACITY_LOWER_THAN_ENROLLED,
                        String.format("Không thể giảm sức chứa (%d) xuống thấp hơn số sinh viên hiện đã đăng ký (%d)",
                                request.getCapacity(), section.getEnrolledCount()));
            }
            for (ScheduleSlot slot : section.getScheduleSlots()) {
                if (slot.getRoom() != null && slot.getRoom().getCapacity() < request.getCapacity()) {
                    throw new BusinessException(ErrorCode.ERR_ROOM_CAPACITY_INSUFFICIENT,
                            String.format("Sức chứa mới (%d) vượt quá sức chứa phòng %s (%d)",
                                    request.getCapacity(), slot.getRoom().getRoomNumber(), slot.getRoom().getCapacity()));
                }
            }
            section.setCapacity(request.getCapacity());
        }

        section.setSectionCode(newCode);
        CourseSection updated = sectionRepository.save(section);
        log.info("Updated section id={}, code={}", sectionId, newCode);
        return mapToResponse(updated);
    }

    /**
     * UC-09: Xóa Lớp học phần rỗng
     */
    @Transactional
    public void deleteSection(Long sectionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_NOT_FOUND, "Không tìm thấy lớp học phần với ID: " + sectionId));

        if (section.getEnrolledCount() > 0 || enrollmentRecordRepository.existsBySectionId(sectionId)) {
            throw new BusinessException(ErrorCode.ERR_VALIDATION_FAILED,
                    "Không thể xóa lớp học phần đã có sinh viên đăng ký. Vui lòng sử dụng tính năng Hủy lớp.");
        }

        sectionRepository.delete(section);
        log.info("Deleted course section id={}, code={}", sectionId, section.getSectionCode());
    }

    /**
     * Kiểm tra giao thoa khoảng thời gian: [start1, end1] và [start2, end2] giao nhau khi max(s1, s2) <= min(e1, e2)
     */
    public boolean isTimeOverlap(int start1, int end1, int start2, int end2) {
        return Math.max(start1, start2) <= Math.min(end1, end2);
    }

    private CourseSectionResponse mapToResponse(CourseSection section) {
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
                .courseId(section.getCourse() != null ? section.getCourse().getId() : null)
                .courseCode(section.getCourse() != null ? section.getCourse().getCode() : null)
                .courseName(section.getCourse() != null ? section.getCourse().getName() : null)
                .totalCredits(section.getCourse() != null ? section.getCourse().getTotalCredits() : null)
                .semesterId(section.getSemester() != null ? section.getSemester().getId() : null)
                .semesterName(section.getSemester() != null ? section.getSemester().getName() : null)
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
