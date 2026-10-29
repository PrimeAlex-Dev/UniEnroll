package com.unienroll.config;

import com.unienroll.modules.auth.entity.*;
import com.unienroll.modules.auth.repository.LecturerProfileRepository;
import com.unienroll.modules.auth.repository.StaffProfileRepository;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!test")
    public CommandLineRunner initDatabaseSeed() {
        return args -> {
            log.info("Initializing baseline user accounts for 4 roles...");

            // 1. Quản trị viên hệ thống (Admin)
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("123456"))
                        .email("admin@unienroll.edu.vn")
                        .role(Role.ROLE_ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build());
                log.info("Initialized default ADMIN account: username='admin', password='123456'");
            }

            // 2. Cán bộ Phòng Đào tạo (Staff)
            if (!userRepository.existsByUsername("staff")) {
                User staffUser = userRepository.save(User.builder()
                        .username("staff")
                        .password(passwordEncoder.encode("123456"))
                        .email("staff@unienroll.edu.vn")
                        .role(Role.ROLE_STAFF)
                        .status(UserStatus.ACTIVE)
                        .build());
                staffProfileRepository.save(StaffProfile.builder()
                        .staffCode("CB001")
                        .fullName("Nguyễn Văn Đào Tạo")
                        .user(staffUser)
                        .build());
                log.info("Initialized default STAFF account: username='staff', password='123456'");
            }

            // 3. Giảng viên (Lecturer)
            if (!userRepository.existsByUsername("lecturer")) {
                User lecturerUser = userRepository.save(User.builder()
                        .username("lecturer")
                        .password(passwordEncoder.encode("123456"))
                        .email("lecturer@unienroll.edu.vn")
                        .role(Role.ROLE_LECTURER)
                        .status(UserStatus.ACTIVE)
                        .build());
                lecturerProfileRepository.save(LecturerProfile.builder()
                        .lecturerCode("GV001")
                        .fullName("TS. Nguyễn Văn Giảng")
                        .departmentName("Khoa Công nghệ Thông tin")
                        .user(lecturerUser)
                        .build());
                log.info("Initialized default LECTURER account: username='lecturer', password='123456'");
            }

            // 4. Sinh viên (Student)
            if (!userRepository.existsByUsername("student")) {
                User studentUser = userRepository.save(User.builder()
                        .username("student")
                        .password(passwordEncoder.encode("123456"))
                        .email("student@unienroll.edu.vn")
                        .role(Role.ROLE_STUDENT)
                        .status(UserStatus.ACTIVE)
                        .build());
                studentProfileRepository.save(StudentProfile.builder()
                        .studentCode("SV20260001")
                        .fullName("Trần Văn Sinh")
                        .cohort("K65")
                        .major("Kỹ thuật Phần mềm")
                        .isCreditLimitOverridden(true)
                        .user(studentUser)
                        .build());
                log.info("Initialized default STUDENT account: username='student', password='123456'");
            }

            log.info("Baseline user accounts initialization completed successfully (Only 4 accounts).");
        };
    }
}
