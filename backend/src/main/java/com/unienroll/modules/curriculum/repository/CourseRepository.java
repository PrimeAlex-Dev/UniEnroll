package com.unienroll.modules.curriculum.repository;

import com.unienroll.modules.curriculum.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"department"})
    List<Course> findByDepartmentId(Long departmentId);

    @Override
    @EntityGraph(attributePaths = {"department"})
    List<Course> findAll();

    @Override
    @EntityGraph(attributePaths = {"department"})
    Optional<Course> findById(Long id);
}
