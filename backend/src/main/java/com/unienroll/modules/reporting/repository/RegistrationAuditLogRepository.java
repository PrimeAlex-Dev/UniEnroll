package com.unienroll.modules.reporting.repository;

import com.unienroll.modules.reporting.entity.AuditActionType;
import com.unienroll.modules.reporting.entity.RegistrationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RegistrationAuditLogRepository extends JpaRepository<RegistrationAuditLog, Long>, JpaSpecificationExecutor<RegistrationAuditLog> {

    List<RegistrationAuditLog> findByActorIdOrderByTimestampDesc(Long actorId);

    List<RegistrationAuditLog> findByTargetSectionIdOrderByTimestampDesc(Long targetSectionId);

    List<RegistrationAuditLog> findByActionTypeOrderByTimestampDesc(AuditActionType actionType);

    List<RegistrationAuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end);

    List<RegistrationAuditLog> findAllByOrderByTimestampDesc();
}
