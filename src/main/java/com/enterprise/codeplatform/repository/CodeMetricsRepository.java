package com.enterprise.codeplatform.repository;

import com.enterprise.codeplatform.entity.CodeMetrics;
import com.enterprise.codeplatform.entity.CodeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeMetricsRepository extends JpaRepository<CodeMetrics, Long> {
    void deleteByVersion(CodeVersion version);
}
