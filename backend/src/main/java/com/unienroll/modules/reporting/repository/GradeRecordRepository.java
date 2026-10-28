package com.unienroll.modules.reporting.repository;

import com.unienroll.modules.reporting.entity.GradeRecord;
import com.unienroll.modules.reporting.entity.GradeStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRecordRepository extends JpaRepository<GradeRecord, Long> {

    @EntityGraph(attributePaths = {"course", "semester", "student"})
    List<GradeRecord> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"course", "semester", "student"})
    Optional<GradeRecord> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, GradeStatus status);
}
