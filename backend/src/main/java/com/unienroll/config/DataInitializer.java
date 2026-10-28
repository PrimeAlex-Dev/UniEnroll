package com.unienroll.config;

import com.unienroll.modules.auth.entity.*;
import com.unienroll.modules.auth.repository.LecturerProfileRepository;
import com.unienroll.modules.auth.repository.StaffProfileRepository;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.modules.curriculum.entity.Course;
import com.unienroll.modules.curriculum.entity.Department;
import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import com.unienroll.modules.curriculum.repository.CourseRepository;
import com.unienroll.modules.curriculum.repository.DepartmentRepository;
import com.unienroll.modules.curriculum.repository.PrerequisiteConditionRepository;
import com.unienroll.modules.engine.entity.RegistrationPhase;
import com.unienroll.modules.engine.repository.RegistrationPhaseRepository;
import com.unienroll.modules.scheduling.entity.*;
import com.unienroll.modules.scheduling.repository.CourseSectionRepository;
import com.unienroll.modules.scheduling.repository.RoomRepository;
import com.unienroll.modules.scheduling.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final PrerequisiteConditionRepository prerequisiteRepository;
    private final SemesterRepository semesterRepository;
    private final RoomRepository roomRepository;
    private final CourseSectionRepository sectionRepository;
    private final RegistrationPhaseRepository phaseRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!test")
    public CommandLineRunner initDatabaseSeed() {
        return args -> {
            // 1. Khởi tạo Users & Profiles (4 Roles)
            if (!userRepository.existsByUsername("admin")) {
                User admin = userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .email("admin@unienroll.edu.vn")
                        .role(Role.ROLE_ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build());
                log.info("Initialized default ADMIN account: username='admin', password='Admin@123'");
            }

            if (!userRepository.existsByUsername("staff")) {
                User staffUser = userRepository.save(User.builder()
                        .username("staff")
                        .password(passwordEncoder.encode("Staff@123"))
                        .email("staff@unienroll.edu.vn")
                        .role(Role.ROLE_STAFF)
                        .status(UserStatus.ACTIVE)
                        .build());
                staffProfileRepository.save(StaffProfile.builder()
                        .staffCode("CB001")
                        .fullName("Nguyễn Văn Đào Tạo")
                        .user(staffUser)
                        .build());
                log.info("Initialized default STAFF account: username='staff', password='Staff@123'");
            }

            LecturerProfile defaultLecturer = null;
            if (!userRepository.existsByUsername("lecturer")) {
                User lecturerUser = userRepository.save(User.builder()
                        .username("lecturer")
                        .password(passwordEncoder.encode("Lecturer@123"))
                        .email("lecturer@unienroll.edu.vn")
                        .role(Role.ROLE_LECTURER)
                        .status(UserStatus.ACTIVE)
                        .build());
                defaultLecturer = lecturerProfileRepository.save(LecturerProfile.builder()
                        .lecturerCode("GV001")
                        .fullName("TS. Nguyễn Văn Giảng")
                        .departmentName("Khoa Công nghệ Thông tin")
                        .user(lecturerUser)
                        .build());
                log.info("Initialized default LECTURER account: username='lecturer', password='Lecturer@123'");
            } else {
                defaultLecturer = lecturerProfileRepository.findByLecturerCode("GV001").orElse(null);
            }

            if (!userRepository.existsByUsername("student")) {
                User studentUser = userRepository.save(User.builder()
                        .username("student")
                        .password(passwordEncoder.encode("Student@123"))
                        .email("student@unienroll.edu.vn")
                        .role(Role.ROLE_STUDENT)
                        .status(UserStatus.ACTIVE)
                        .build());
                studentProfileRepository.save(StudentProfile.builder()
                        .studentCode("SV20260001")
                        .fullName("Trần Văn Sinh")
                        .cohort("K65")
                        .major("Kỹ thuật Phần mềm")
                        .user(studentUser)
                        .build());
                log.info("Initialized default STUDENT account: username='student', password='Student@123'");
            }

            // 2. Khởi tạo Khoa
            Department itDept;
            if (!departmentRepository.existsByCode("CS")) {
                itDept = departmentRepository.save(Department.builder()
                        .code("CS")
                        .name("Khoa Công nghệ Thông tin")
                        .description("Đào tạo Kỹ thuật Phần mềm, Khoa học Máy tính")
                        .build());
            } else {
                itDept = departmentRepository.findByCode("CS").orElse(null);
            }

            Department mathDept;
            if (!departmentRepository.existsByCode("MATH")) {
                mathDept = departmentRepository.save(Department.builder()
                        .code("MATH")
                        .name("Khoa Toán - Tin học")
                        .description("Đào tạo Toán ứng dụng, Thống kê dữ liệu")
                        .build());
            } else {
                mathDept = departmentRepository.findByCode("MATH").orElse(null);
            }

            // 3. Khởi tạo Môn học & Tiên quyết
            Course cs101 = courseRepository.findByCode("CS101").orElseGet(() ->
                    courseRepository.save(Course.builder()
                            .code("CS101")
                            .name("Nhập môn Lập trình")
                            .theoryCredits(3)
                            .practicalCredits(0)
                            .totalCredits(3)
                            .department(itDept)
                            .build()));

            Course cs102 = courseRepository.findByCode("CS102").orElseGet(() ->
                    courseRepository.save(Course.builder()
                            .code("CS102")
                            .name("Cấu trúc Dữ liệu & Giải thuật")
                            .theoryCredits(3)
                            .practicalCredits(0)
                            .totalCredits(3)
                            .department(itDept)
                            .build()));

            Course cs103 = courseRepository.findByCode("CS103").orElseGet(() ->
                    courseRepository.save(Course.builder()
                            .code("CS103")
                            .name("Cơ sở Dữ liệu")
                            .theoryCredits(3)
                            .practicalCredits(0)
                            .totalCredits(3)
                            .department(itDept)
                            .build()));

            Course cs104 = courseRepository.findByCode("CS104").orElseGet(() ->
                    courseRepository.save(Course.builder()
                            .code("CS104")
                            .name("Lập trình Web")
                            .theoryCredits(3)
                            .practicalCredits(0)
                            .totalCredits(3)
                            .department(itDept)
                            .build()));

            Course mth101 = courseRepository.findByCode("MTH101").orElseGet(() ->
                    courseRepository.save(Course.builder()
                            .code("MTH101")
                            .name("Giải tích 1")
                            .theoryCredits(3)
                            .practicalCredits(0)
                            .totalCredits(3)
                            .department(mathDept)
                            .build()));

            // Gán Tiên quyết: CS102 yêu cầu CS101, CS103 yêu cầu CS101, CS104 yêu cầu CS102 & CS103
            if (!prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(cs102.getId(), cs101.getId())) {
                prerequisiteRepository.save(PrerequisiteCondition.builder().targetCourse(cs102).prerequisiteCourse(cs101).build());
            }
            if (!prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(cs103.getId(), cs101.getId())) {
                prerequisiteRepository.save(PrerequisiteCondition.builder().targetCourse(cs103).prerequisiteCourse(cs101).build());
            }
            if (!prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(cs104.getId(), cs102.getId())) {
                prerequisiteRepository.save(PrerequisiteCondition.builder().targetCourse(cs104).prerequisiteCourse(cs102).build());
            }
            if (!prerequisiteRepository.existsByTargetCourseIdAndPrerequisiteCourseId(cs104.getId(), cs103.getId())) {
                prerequisiteRepository.save(PrerequisiteCondition.builder().targetCourse(cs104).prerequisiteCourse(cs103).build());
            }

            // 4. Khởi tạo Học kỳ
            Semester activeSemester = semesterRepository.findByCode("2026_HK1").orElseGet(() ->
                    semesterRepository.save(Semester.builder()
                            .code("2026_HK1")
                            .name("Học kỳ 1 Năm học 2026-2027")
                            .startDate(LocalDate.of(2026, 9, 1))
                            .endDate(LocalDate.of(2027, 1, 15))
                            .status(SemesterStatus.ACTIVE)
                            .build()));

            // 5. Khởi tạo Phòng học
            Room roomA101 = roomRepository.findByRoomNumber("A101").orElseGet(() ->
                    roomRepository.save(Room.builder().building("Tòa A").roomNumber("A101").capacity(50).build()));
            Room roomA102 = roomRepository.findByRoomNumber("A102").orElseGet(() ->
                    roomRepository.save(Room.builder().building("Tòa A").roomNumber("A102").capacity(40).build()));
            Room roomB201 = roomRepository.findByRoomNumber("B201").orElseGet(() ->
                    roomRepository.save(Room.builder().building("Tòa B").roomNumber("B201").capacity(60).build()));
            Room roomB202 = roomRepository.findByRoomNumber("B202").orElseGet(() ->
                    roomRepository.save(Room.builder().building("Tòa B").roomNumber("B202").capacity(40).build()));
            Room roomC301 = roomRepository.findByRoomNumber("C301").orElseGet(() ->
                    roomRepository.save(Room.builder().building("Tòa C").roomNumber("C301").capacity(80).build()));

            // 6. Khởi tạo Đợt đăng ký
            if (phaseRepository.findBySemesterId(activeSemester.getId()).isEmpty()) {
                phaseRepository.save(RegistrationPhase.builder()
                        .semester(activeSemester)
                        .name("Đợt 1 - Đăng ký Học phần Chính thức HK1 2026")
                        .startTime(Instant.now().minus(1, ChronoUnit.DAYS))
                        .endTime(Instant.now().plus(14, ChronoUnit.DAYS))
                        .build());
            }

            // 7. Khởi tạo Lớp học phần kèm Lịch học
            if (!sectionRepository.existsBySemesterIdAndSectionCode(activeSemester.getId(), "CS101.N01")) {
                CourseSection sec1 = CourseSection.builder()
                        .sectionCode("CS101.N01")
                        .course(cs101)
                        .semester(activeSemester)
                        .lecturer(defaultLecturer)
                        .capacity(50)
                        .status(SectionStatus.OPEN)
                        .build();
                ScheduleSlot slot1 = ScheduleSlot.builder()
                        .section(sec1)
                        .room(roomA101)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startPeriod(1)
                        .endPeriod(3)
                        .build();
                sec1.setScheduleSlots(List.of(slot1));
                sectionRepository.save(sec1);
            }

            if (!sectionRepository.existsBySemesterIdAndSectionCode(activeSemester.getId(), "CS101.N02")) {
                CourseSection sec2 = CourseSection.builder()
                        .sectionCode("CS101.N02")
                        .course(cs101)
                        .semester(activeSemester)
                        .lecturer(defaultLecturer)
                        .capacity(40)
                        .status(SectionStatus.OPEN)
                        .build();
                ScheduleSlot slot2 = ScheduleSlot.builder()
                        .section(sec2)
                        .room(roomA102)
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .startPeriod(4)
                        .endPeriod(6)
                        .build();
                sec2.setScheduleSlots(List.of(slot2));
                sectionRepository.save(sec2);
            }

            if (!sectionRepository.existsBySemesterIdAndSectionCode(activeSemester.getId(), "CS102.N01")) {
                CourseSection sec3 = CourseSection.builder()
                        .sectionCode("CS102.N01")
                        .course(cs102)
                        .semester(activeSemester)
                        .lecturer(defaultLecturer)
                        .capacity(50)
                        .status(SectionStatus.OPEN)
                        .build();
                ScheduleSlot slot3 = ScheduleSlot.builder()
                        .section(sec3)
                        .room(roomB201)
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .startPeriod(1)
                        .endPeriod(3)
                        .build();
                sec3.setScheduleSlots(List.of(slot3));
                sectionRepository.save(sec3);
            }

            if (!sectionRepository.existsBySemesterIdAndSectionCode(activeSemester.getId(), "MTH101.N01")) {
                CourseSection sec4 = CourseSection.builder()
                        .sectionCode("MTH101.N01")
                        .course(mth101)
                        .semester(activeSemester)
                        .capacity(60)
                        .status(SectionStatus.OPEN)
                        .build();
                ScheduleSlot slot4 = ScheduleSlot.builder()
                        .section(sec4)
                        .room(roomC301)
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .startPeriod(1)
                        .endPeriod(3)
                        .build();
                sec4.setScheduleSlots(List.of(slot4));
                sectionRepository.save(sec4);
            }

            log.info("Finished Database Seed Initialization successfully!");
        };
    }
}
