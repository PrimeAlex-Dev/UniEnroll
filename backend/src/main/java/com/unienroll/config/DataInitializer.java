package com.unienroll.config;

import com.unienroll.modules.auth.entity.Role;
import com.unienroll.modules.auth.entity.StaffProfile;
import com.unienroll.modules.auth.entity.User;
import com.unienroll.modules.auth.entity.UserStatus;
import com.unienroll.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDefaultAdmin() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("Admin@123"))
                        .email("admin@unienroll.edu.vn")
                        .role(Role.ROLE_ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();

                userRepository.save(admin);
                log.info("Initialized default ADMIN account: username='admin', password='Admin@123'");
            }
        };
    }
}
