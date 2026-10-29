package com.unienroll.config;

import com.unienroll.security.CustomAccessDeniedHandler;
import com.unienroll.security.CustomAuthenticationEntryPoint;
import com.unienroll.security.CustomUserDetailsService;
import com.unienroll.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/h2-console/**",
                                "/error"
                        ).permitAll()
                        // UC-04: Quản trị Người dùng (POST/PUT chỉ dành riêng cho ADMIN; GET cho ADMIN & STAFF để tra cứu giảng viên/sinh viên)
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/users/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")

                        // Dữ liệu danh mục chung (Học kỳ, Phòng học, Đợt đăng ký, Lớp học phần, Khoa, Môn học) cho phép TẤT CẢ user đã đăng nhập đọc (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/semesters/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/rooms/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/registration-phases/**", "/api/v1/academic/phases/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/sections/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/departments/**", "/api/v1/curriculum/departments/**", "/api/v1/academic/departments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**", "/api/v1/curriculum/courses/**", "/api/v1/academic/courses/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/sections/**", "/api/v1/catalog/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/academic/grades/student/**").authenticated()

                        // UC-05: Quản lý Khoa / Bộ môn (Tác tử: Academic Staff & Admin)
                        .requestMatchers("/api/v1/admin/departments/**", "/api/v1/curriculum/departments/**", "/api/v1/academic/departments/**").hasAnyRole("ADMIN", "STAFF")

                        // UC-19: Tra cứu Nhật ký Kiểm toán Đăng ký (Tác tử: System Administrator & Academic Staff)
                        .requestMatchers("/api/v1/admin/audit-logs/**").hasAnyRole("ADMIN", "STAFF")

                        // UC-06 đến UC-12, UC-18: Các thao tác nghiệp vụ đào tạo (Môn học, Học kỳ, Phòng học, Lớp học phần, Đợt đăng ký, Nhập điểm, Báo cáo lấp đầy):
                        // CHỈ DÀNH RIÊNG CHO CÁN BỘ ĐÀO TẠO (ROLE_STAFF) THEO ĐÚNG TÀI LIỆU ĐẶC TẢ USE CASE!
                        .requestMatchers("/api/v1/academic/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/courses/**", "/api/v1/curriculum/courses/**", "/api/v1/academic/courses/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/reports/**").hasRole("STAFF")

                        // Phân hệ Giảng viên (UC-17: Roster)
                        .requestMatchers("/api/v1/lecturer/**").hasAnyRole("LECTURER", "STAFF")

                        // Phân hệ Sinh viên (UC-14, 15, 16: Core Transactions)
                        .requestMatchers("/api/v1/student/**", "/api/v1/registration/**").hasRole("STUDENT")

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
