package com.unienroll.modules.auth.dto;

import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private UserStatus status;
    private Map<String, Object> studentProfile;
    private Map<String, Object> lecturerProfile;
    private Map<String, Object> staffProfile;
}
