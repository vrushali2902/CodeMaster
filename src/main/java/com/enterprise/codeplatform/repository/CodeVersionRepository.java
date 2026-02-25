package com.enterprise.codeplatform.repository;

import com.enterprise.codeplatform.entity.CodeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeVersionRepository extends JpaRepository<CodeVersion, Long> {
    List<CodeVersion> findBySnippetIdOrderByVersionNumberDesc(Long snippetId);

    CodeVersion findBySnippetIdAndVersionNumber(Long snippetId, int versionNumber);
}
