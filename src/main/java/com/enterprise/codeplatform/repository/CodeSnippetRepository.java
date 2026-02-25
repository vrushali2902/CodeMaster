package com.enterprise.codeplatform.repository;

import com.enterprise.codeplatform.entity.CodeSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {
    List<CodeSnippet> findByAuthorId(Long authorId);
}
