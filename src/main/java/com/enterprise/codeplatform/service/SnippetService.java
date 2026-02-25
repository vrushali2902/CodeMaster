package com.enterprise.codeplatform.service;

import com.enterprise.codeplatform.dto.SnippetRequest;
import com.enterprise.codeplatform.dto.SnippetResponse;
import com.enterprise.codeplatform.entity.CodeSnippet;
import com.enterprise.codeplatform.entity.CodeVersion;
import com.enterprise.codeplatform.entity.User;
import com.enterprise.codeplatform.repository.AuditLogRepository;
import com.enterprise.codeplatform.repository.CodeMetricsRepository;
import com.enterprise.codeplatform.repository.CodeSnippetRepository;
import com.enterprise.codeplatform.repository.CodeVersionRepository;
import com.enterprise.codeplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SnippetService {

    private final CodeSnippetRepository snippetRepository;
    private final CodeVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    private final CodeMetricsRepository metricsRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public SnippetResponse createSnippet(SnippetRequest request, String username) {
        User author = userRepository.findByEmail(username).orElseThrow();

        CodeSnippet snippet = CodeSnippet.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .currentContent(request.getContent())
                .language(request.getLanguage())
                .author(author)
                .activeVersionNumber(1)
                .build();

        snippet = snippetRepository.save(snippet);

        createVersionEntry(snippet, request.getContent(), 1, "Initial version", username);

        return mapToResponse(snippet);
    }

    @Transactional
    public SnippetResponse updateSnippet(Long id, SnippetRequest request, String username) {
        CodeSnippet snippet = snippetRepository.findById(id).orElseThrow();
        checkOwnership(snippet, username);

        int nextVersion = snippet.getActiveVersionNumber() + 1;
        snippet.setCurrentContent(request.getContent());
        snippet.setActiveVersionNumber(nextVersion);
        if (request.getTitle() != null)
            snippet.setTitle(request.getTitle());
        if (request.getDescription() != null)
            snippet.setDescription(request.getDescription());

        snippet = snippetRepository.save(snippet);

        createVersionEntry(snippet, request.getContent(), nextVersion, "Updated version " + nextVersion, username);

        return mapToResponse(snippet);
    }

    @Transactional
    public SnippetResponse rollback(Long id, int versionNumber, String username) {
        CodeSnippet snippet = snippetRepository.findById(id).orElseThrow();
        checkOwnership(snippet, username);

        CodeVersion version = versionRepository.findBySnippetIdAndVersionNumber(id, versionNumber);

        if (version == null)
            throw new RuntimeException("Version not found");

        int nextVersion = snippet.getActiveVersionNumber() + 1;
        snippet.setCurrentContent(version.getContent());
        snippet.setActiveVersionNumber(nextVersion);

        snippet = snippetRepository.save(snippet);

        createVersionEntry(snippet, version.getContent(), nextVersion, "Rolled back to version " + versionNumber,
                username);

        return mapToResponse(snippet);
    }

    public List<SnippetResponse> getAllSnippets(String username) {
        User user = userRepository.findByEmail(username).orElseThrow();
        return snippetRepository.findByAuthorId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SnippetResponse getSnippet(Long id, String username) {
        CodeSnippet snippet = snippetRepository.findById(id).orElseThrow();
        checkOwnership(snippet, username);
        return mapToResponse(snippet);
    }

    public String getVersionContent(Long snippetId, int versionNumber, String username) {
        CodeSnippet snippet = snippetRepository.findById(snippetId).orElseThrow();
        checkOwnership(snippet, username);

        CodeVersion version = versionRepository.findBySnippetIdAndVersionNumber(snippetId, versionNumber);
        if (version == null)
            throw new RuntimeException("Version not found");
        return version.getContent();
    }

    private void createVersionEntry(CodeSnippet snippet, String content, int versionNum, String message,
            String username) {
        CodeVersion version = CodeVersion.builder()
                .snippet(snippet)
                .content(content)
                .versionNumber(versionNum)
                .commitMessage(message)
                .build();
        version = versionRepository.save(version);

        // Calculate and save metrics
        com.enterprise.codeplatform.entity.CodeMetrics metrics = metricsService.calculateMetrics(version);
        metricsRepository.save(metrics);

        // Log audit trail
        com.enterprise.codeplatform.entity.AuditLog log = com.enterprise.codeplatform.entity.AuditLog.builder()
                .action("VERSION_CREATED")
                .entityName("CodeSnippet")
                .entityId(snippet.getId())
                .performBy(username)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public void deleteVersion(Long snippetId, int versionNumber, String username) {
        CodeSnippet snippet = snippetRepository.findById(snippetId).orElseThrow();
        checkOwnership(snippet, username);

        CodeVersion version = versionRepository.findBySnippetIdAndVersionNumber(snippetId, versionNumber);

        if (version == null)
            throw new RuntimeException("Version not found");

        if (snippet.getActiveVersionNumber() == versionNumber) {
            throw new RuntimeException("Cannot delete the active version. Rollback to another version first.");
        }

        // Delete associated metrics first to avoid FK constraint violation
        metricsRepository.deleteByVersion(version);

        versionRepository.delete(version);

        // Log audit trail for deletion
        com.enterprise.codeplatform.entity.AuditLog log = com.enterprise.codeplatform.entity.AuditLog.builder()
                .action("VERSION_DELETED")
                .entityName("CodeVersion")
                .entityId(version.getId())
                .performBy(username)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public void deleteVersionById(Long versionId, String username) {
        CodeVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        CodeSnippet snippet = version.getSnippet();
        checkOwnership(snippet, username);

        if (snippet.getActiveVersionNumber() == version.getVersionNumber()) {
            throw new RuntimeException("Cannot delete the active version. Rollback to another version first.");
        }

        // Delete associated metrics first to avoid FK constraint violation
        metricsRepository.deleteByVersion(version);

        versionRepository.delete(version);

        // Log audit trail for deletion
        com.enterprise.codeplatform.entity.AuditLog log = com.enterprise.codeplatform.entity.AuditLog.builder()
                .action("VERSION_DELETED_BY_ID")
                .entityName("CodeVersion")
                .entityId(versionId)
                .performBy(username)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<com.enterprise.codeplatform.dto.VersionResponse> getSnippetVersions(Long snippetId, String username) {
        CodeSnippet snippet = snippetRepository.findById(snippetId).orElseThrow();
        checkOwnership(snippet, username);

        return versionRepository.findBySnippetIdOrderByVersionNumberDesc(snippetId).stream()
                .map(v -> com.enterprise.codeplatform.dto.VersionResponse.builder()
                        .id(v.getId())
                        .versionNumber(v.getVersionNumber())
                        .content(v.getContent())
                        .commitMessage(v.getCommitMessage())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSnippet(Long id, String username) {
        CodeSnippet snippet = snippetRepository.findById(id).orElseThrow();
        checkOwnership(snippet, username);

        // Delete all metrics associated with all versions of this snippet
        for (CodeVersion version : snippet.getVersions()) {
            metricsRepository.deleteByVersion(version);
        }

        snippetRepository.delete(snippet);

        // Log audit trail for snippet deletion
        com.enterprise.codeplatform.entity.AuditLog log = com.enterprise.codeplatform.entity.AuditLog.builder()
                .action("SNIPPET_DELETED")
                .entityName("CodeSnippet")
                .entityId(id)
                .performBy(username)
                .build();
        auditLogRepository.save(log);
    }

    private void checkOwnership(CodeSnippet snippet, String email) {
        if (!snippet.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("Access Denied: You do not own this snippet.");
        }
    }

    private SnippetResponse mapToResponse(CodeSnippet s) {
        return SnippetResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .currentContent(s.getCurrentContent())
                .language(s.getLanguage())
                .activeVersionNumber(s.getActiveVersionNumber())
                .authorName(s.getAuthor().getDisplayUsername())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
