package com.unienroll.modules.auth.repository;

import com.unienroll.modules.auth.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    Optional<StaffProfile> findByStaffCode(String staffCode);

    Optional<StaffProfile> findByUserId(Long userId);

    boolean existsByStaffCode(String staffCode);
}
