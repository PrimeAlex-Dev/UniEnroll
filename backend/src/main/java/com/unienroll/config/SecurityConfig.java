package com.unienroll.config;

import com.unienroll.security.CustomAccessDeniedHandler;
import com.unienroll.security.CustomAuthenticationEntryPoint;
import com.unienroll.security.CustomUserDetailsService;
import com.unienroll.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/h2-console/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/admin/departments/**").authenticated()
                        .requestMatchers("/api/v1/admin/departments/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/academic/grades/student/**").authenticated()
                        .requestMatchers("/api/v1/academic/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/lecturer/**").hasAnyRole("LECTURER", "STAFF", "ADMIN")
                        .requestMatchers("/api/v1/admin/audit-logs/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/v1/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/v1/sections/**").authenticated()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // for H2 Console
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
