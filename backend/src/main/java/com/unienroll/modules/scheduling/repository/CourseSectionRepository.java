package com.unienroll.modules.scheduling.repository;

import com.unienroll.modules.scheduling.entity.CourseSection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    boolean existsBySemesterIdAndSectionCode(Long semesterId, String sectionCode);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "scheduleSlots", "scheduleSlots.room"})
    @Override
    Optional<CourseSection> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CourseSection s WHERE s.id = :id")
    Optional<CourseSection> findByIdWithPessimisticLock(@Param("id") Long id);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "scheduleSlots", "scheduleSlots.room"})
    List<CourseSection> findBySemesterId(Long semesterId);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "scheduleSlots", "scheduleSlots.room"})
    List<CourseSection> findBySemesterIdAndCourseId(Long semesterId, Long courseId);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "scheduleSlots", "scheduleSlots.room"})
    List<CourseSection> findBySemesterIdAndCourseDepartmentId(Long semesterId, Long departmentId);

    @EntityGraph(attributePaths = {"course", "semester", "lecturer", "scheduleSlots", "scheduleSlots.room"})
    List<CourseSection> findByLecturerId(Long lecturerId);
}
