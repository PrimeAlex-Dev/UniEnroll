package com.unienroll.modules.scheduling.repository;

import com.unienroll.modules.scheduling.entity.ScheduleSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {

    @EntityGraph(attributePaths = {"room", "section"})
    List<ScheduleSlot> findBySectionId(Long sectionId);

    @EntityGraph(attributePaths = {"room", "section"})
    @Query("SELECT s FROM ScheduleSlot s WHERE s.section.semester.id = :semesterId AND s.room.id = :roomId AND s.dayOfWeek = :dayOfWeek")
    List<ScheduleSlot> findExistingSlotsByRoom(
            @Param("semesterId") Long semesterId,
            @Param("roomId") Long roomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    @EntityGraph(attributePaths = {"room", "section"})
    @Query("SELECT s FROM ScheduleSlot s WHERE s.section.semester.id = :semesterId AND s.section.lecturer.id = :lecturerId AND s.dayOfWeek = :dayOfWeek")
    List<ScheduleSlot> findExistingSlotsByLecturer(
            @Param("semesterId") Long semesterId,
            @Param("lecturerId") Long lecturerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
}
