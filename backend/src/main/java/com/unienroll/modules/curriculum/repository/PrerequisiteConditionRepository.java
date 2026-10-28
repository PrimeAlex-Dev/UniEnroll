package com.unienroll.modules.curriculum.repository;

import com.unienroll.modules.curriculum.entity.PrerequisiteCondition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrerequisiteConditionRepository extends JpaRepository<PrerequisiteCondition, Long> {

    @EntityGraph(attributePaths = {"prerequisiteCourse"})
    List<PrerequisiteCondition> findByTargetCourseId(Long targetCourseId);

    @EntityGraph(attributePaths = {"targetCourse"})
    List<PrerequisiteCondition> findByPrerequisiteCourseId(Long prerequisiteCourseId);

    boolean existsByTargetCourseIdAndPrerequisiteCourseId(Long targetCourseId, Long prerequisiteCourseId);

    void deleteByTargetCourseIdAndPrerequisiteCourseId(Long targetCourseId, Long prerequisiteCourseId);

    void deleteByTargetCourseId(Long targetCourseId);
}
