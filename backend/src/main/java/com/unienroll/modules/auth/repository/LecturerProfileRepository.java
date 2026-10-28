package com.unienroll.modules.auth.repository;

import com.unienroll.modules.auth.entity.LecturerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Long> {

    Optional<LecturerProfile> findByLecturerCode(String lecturerCode);

    Optional<LecturerProfile> findByUserId(Long userId);

    boolean existsByLecturerCode(String lecturerCode);
}
