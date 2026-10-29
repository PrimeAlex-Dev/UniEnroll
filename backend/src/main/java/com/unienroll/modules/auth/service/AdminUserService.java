package com.unienroll.modules.auth.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.dto.CreateUserRequest;
import com.unienroll.modules.auth.dto.UserSummaryDto;
import com.unienroll.modules.auth.entity.*;
import com.unienroll.modules.auth.repository.LecturerProfileRepository;
import com.unienroll.modules.auth.repository.StaffProfileRepository;
import com.unienroll.modules.auth.repository.StudentProfileRepository;
import com.unienroll.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final LecturerProfileRepository lecturerProfileRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSummaryDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.ERR_USER_ALREADY_EXISTS, "Tên đăng nhập đã tồn tại: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.ERR_USER_ALREADY_EXISTS, "Email đã tồn tại: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        switch (request.getRole()) {
            case ROLE_STUDENT -> {
                String code = StringUtils.hasText(request.getStudentCode()) ? request.getStudentCode() : "STU_" + request.getUsername();
                if (studentProfileRepository.existsByStudentCode(code)) {
                    throw new BusinessException(ErrorCode.ERR_USER_ALREADY_EXISTS, "Mã sinh viên đã tồn tại: " + code);
                }
                StudentProfile profile = StudentProfile.builder()
                        .studentCode(code)
                        .fullName(request.getFullName())
                        .cohort(StringUtils.hasText(request.getCohort()) ? request.getCohort() : "K2026")
                        .major(request.getMajor())
                        .user(user)
                        .build();
                user.setStudentProfile(profile);
            }
            case ROLE_LECTURER -> {
                String code = StringUtils.hasText(request.getLecturerCode()) ? request.getLecturerCode() : "LEC_" + request.getUsername();
                if (lecturerProfileRepository.existsByLecturerCode(code)) {
                    throw new BusinessException(ErrorCode.ERR_USER_ALREADY_EXISTS, "Mã giảng viên đã tồn tại: " + code);
                }
                LecturerProfile profile = LecturerProfile.builder()
                        .lecturerCode(code)
                        .fullName(request.getFullName())
                        .departmentName(request.getDepartmentName())
                        .user(user)
                        .build();
                user.setLecturerProfile(profile);
            }
            case ROLE_STAFF -> {
                String code = StringUtils.hasText(request.getStaffCode()) ? request.getStaffCode() : "STAFF_" + request.getUsername();
                if (staffProfileRepository.existsByStaffCode(code)) {
                    throw new BusinessException(ErrorCode.ERR_USER_ALREADY_EXISTS, "Mã cán bộ đào tạo đã tồn tại: " + code);
                }
                StaffProfile profile = StaffProfile.builder()
                        .staffCode(code)
                        .fullName(request.getFullName())
                        .user(user)
                        .build();
                user.setStaffProfile(profile);
            }
            case ROLE_ADMIN -> {
                // Admin does not require extra domain profile
            }
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully created user: id={}, username={}, role={}", savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

        return UserSummaryDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .build();
    }

    @Transactional
    public UserSummaryDto updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_USER_NOT_FOUND, "Không tìm thấy người dùng với ID: " + userId));

        user.setStatus(status);
        User updated = userRepository.save(user);
        log.info("Updated status for user id={} to {}", userId, status);

        return UserSummaryDto.builder()
                .id(updated.getId())
                .username(updated.getUsername())
                .email(updated.getEmail())
                .role(updated.getRole())
                .status(updated.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<UserSummaryDto> getAllUsers(String roleStr, String statusStr, String keyword) {
        Role targetRole = null;
        if (StringUtils.hasText(roleStr)) {
            String r = roleStr.trim().toUpperCase();
            if (!r.startsWith("ROLE_")) {
                r = "ROLE_" + r;
            }
            try {
                targetRole = Role.valueOf(r);
            } catch (IllegalArgumentException ignored) {
                if ("ROLE_ACADEMIC_STAFF".equals(r)) {
                    targetRole = Role.ROLE_STAFF;
                }
            }
        }

        UserStatus targetStatus = null;
        if (StringUtils.hasText(statusStr)) {
            try {
                targetStatus = UserStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        final Role filterRole = targetRole;
        final UserStatus filterStatus = targetStatus;

        java.util.List<User> users = userRepository.findAll();
        return users.stream()
                .filter(u -> filterRole == null || u.getRole() == filterRole)
                .filter(u -> filterStatus == null || u.getStatus() == filterStatus)
                .filter(u -> !StringUtils.hasText(keyword) ||
                        u.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                        u.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .map(u -> {
                    String fullName = u.getUsername();
                    java.util.Map<String, Object> stuMap = null;
                    java.util.Map<String, Object> lecMap = null;
                    java.util.Map<String, Object> stfMap = null;

                    if (u.getStudentProfile() != null) {
                        if (StringUtils.hasText(u.getStudentProfile().getFullName())) {
                            fullName = u.getStudentProfile().getFullName();
                        }
                        stuMap = new java.util.HashMap<>();
                        stuMap.put("id", u.getStudentProfile().getId());
                        stuMap.put("studentProfileId", u.getStudentProfile().getId());
                        stuMap.put("studentCode", u.getStudentProfile().getStudentCode());
                        stuMap.put("major", u.getStudentProfile().getMajor() != null ? u.getStudentProfile().getMajor() : "CNTT");
                        stuMap.put("cohort", u.getStudentProfile().getCohort());
                    } else if (u.getLecturerProfile() != null) {
                        if (StringUtils.hasText(u.getLecturerProfile().getFullName())) {
                            fullName = u.getLecturerProfile().getFullName();
                        }
                        lecMap = new java.util.HashMap<>();
                        lecMap.put("id", u.getLecturerProfile().getId());
                        lecMap.put("lecturerProfileId", u.getLecturerProfile().getId());
                        lecMap.put("lecturerCode", u.getLecturerProfile().getLecturerCode());
                        lecMap.put("academicTitle", u.getLecturerProfile().getDepartmentName() != null ? u.getLecturerProfile().getDepartmentName() : "Giảng viên");
                    } else if (u.getStaffProfile() != null) {
                        if (StringUtils.hasText(u.getStaffProfile().getFullName())) {
                            fullName = u.getStaffProfile().getFullName();
                        }
                        stfMap = new java.util.HashMap<>();
                        stfMap.put("id", u.getStaffProfile().getId());
                        stfMap.put("staffProfileId", u.getStaffProfile().getId());
                        stfMap.put("staffCode", u.getStaffProfile().getStaffCode());
                    }

                    return UserSummaryDto.builder()
                            .id(u.getId())
                            .username(u.getUsername())
                            .email(u.getEmail())
                            .fullName(fullName)
                            .role(u.getRole())
                            .status(u.getStatus())
                            .studentProfile(stuMap)
                            .lecturerProfile(lecMap)
                            .staffProfile(stfMap)
                            .build();
                })
                .toList();
    }
}
