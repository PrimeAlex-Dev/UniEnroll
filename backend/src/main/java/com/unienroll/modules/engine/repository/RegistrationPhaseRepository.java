package com.unienroll.modules.engine.repository;

import com.unienroll.modules.engine.entity.RegistrationPhase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RegistrationPhaseRepository extends JpaRepository<RegistrationPhase, Long> {

    @EntityGraph(attributePaths = {"semester"})
    List<RegistrationPhase> findBySemesterId(Long semesterId);

    @EntityGraph(attributePaths = {"semester"})
    @Query("SELECT p FROM RegistrationPhase p WHERE p.semester.id = :semesterId AND :now >= p.startTime AND :now <= p.endTime")
    List<RegistrationPhase> findActivePhases(@Param("semesterId") Long semesterId, @Param("now") Instant now);
}
