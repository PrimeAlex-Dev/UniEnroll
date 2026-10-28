package com.unienroll.modules.auth.repository;

import com.unienroll.modules.auth.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByStudentCode(String studentCode);

    Optional<StudentProfile> findByUserId(Long userId);

    boolean existsByStudentCode(String studentCode);
}
