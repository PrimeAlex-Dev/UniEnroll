package com.unienroll.modules.scheduling.repository;

import com.unienroll.modules.scheduling.entity.Semester;
import com.unienroll.modules.scheduling.entity.SemesterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findByCode(String code);

    boolean existsByCode(String code);

    List<Semester> findByStatus(SemesterStatus status);

    Optional<Semester> findFirstByStatusOrderByStartDateDesc(SemesterStatus status);
}
