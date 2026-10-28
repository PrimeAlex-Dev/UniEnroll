package com.unienroll.modules.auth.service;

import com.unienroll.common.exception.BusinessException;
import com.unienroll.common.exception.ErrorCode;
import com.unienroll.modules.auth.dto.*;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
import com.unienroll.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_AUTH_INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for username: {}", request.getUsername());
            throw new BusinessException(ErrorCode.ERR_AUTH_INVALID_CREDENTIALS);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("Login attempt on locked/inactive account: {}", request.getUsername());
            throw new BusinessException(ErrorCode.ERR_AUTH_ACCOUNT_LOCKED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userSummary)
                .build();
    }

    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.ERR_AUTH_REFRESH_TOKEN_EXPIRED);
        }

        String tokenType = jwtTokenProvider.extractTokenType(token);
        if (!"REFRESH".equals(tokenType)) {
            throw new BusinessException(ErrorCode.ERR_AUTH_REFRESH_TOKEN_EXPIRED, "Token không phải là refresh token hợp lệ");
        }

        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ERR_AUTH_ACCOUNT_LOCKED);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = userRepository.findWithProfilesByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERR_USER_NOT_FOUND));

        Map<String, Object> details = new HashMap<>();
        switch (user.getRole()) {
            case ROLE_STUDENT -> {
                if (user.getStudentProfile() != null) {
                    details.put("studentCode", user.getStudentProfile().getStudentCode());
                    details.put("fullName", user.getStudentProfile().getFullName());
                    details.put("cohort", user.getStudentProfile().getCohort());
                    details.put("major", user.getStudentProfile().getMajor());
                    details.put("isCreditLimitOverridden", user.getStudentProfile().getIsCreditLimitOverridden());
                }
            }
            case ROLE_LECTURER -> {
                if (user.getLecturerProfile() != null) {
                    details.put("lecturerCode", user.getLecturerProfile().getLecturerCode());
                    details.put("fullName", user.getLecturerProfile().getFullName());
                    details.put("departmentName", user.getLecturerProfile().getDepartmentName());
                }
            }
            case ROLE_STAFF -> {
                if (user.getStaffProfile() != null) {
                    details.put("staffCode", user.getStaffProfile().getStaffCode());
                    details.put("fullName", user.getStaffProfile().getFullName());
                }
            }
            case ROLE_ADMIN -> {
                details.put("roleName", "Quản trị viên Hệ thống");
            }
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .profileDetails(details)
                .build();
    }
}
