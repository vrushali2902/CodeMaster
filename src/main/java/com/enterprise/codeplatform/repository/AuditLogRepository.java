package com.enterprise.codeplatform.repository;

import com.enterprise.codeplatform.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
