package com.unienroll.modules.engine.repository;

import com.unienroll.modules.engine.entity.EnrollmentRecord;
import com.unienroll.modules.engine.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRecordRepository extends JpaRepository<EnrollmentRecord, Long> {

    @EntityGraph(attributePaths = {"student", "section", "section.scheduleSlots", "section.scheduleSlots.room", "course", "semester"})
    List<EnrollmentRecord> findByStudentIdAndSemesterIdAndStatus(Long studentId, Long semesterId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {"student", "section", "course", "semester"})
    Optional<EnrollmentRecord> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    @EntityGraph(attributePaths = {"student", "section", "course", "semester"})
    Optional<EnrollmentRecord> findByStudentIdAndSectionIdAndStatus(Long studentId, Long sectionId, EnrollmentStatus status);

    boolean existsByStudentIdAndCourseIdAndSemesterIdAndStatus(Long studentId, Long courseId, Long semesterId, EnrollmentStatus status);

    @EntityGraph(attributePaths = {"student", "section", "course"})
    List<EnrollmentRecord> findBySectionIdAndStatus(Long sectionId, EnrollmentStatus status);

    boolean existsBySectionId(Long sectionId);
}
