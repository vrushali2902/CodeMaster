package com.enterprise.codeplatform.controller;

import com.enterprise.codeplatform.dto.RollbackRequest;
import com.enterprise.codeplatform.dto.SnippetRequest;
import com.enterprise.codeplatform.dto.SnippetResponse;
import com.enterprise.codeplatform.service.SnippetService;
import com.enterprise.codeplatform.service.DiffService;
import com.enterprise.codeplatform.service.CompilerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/snippets")
@RequiredArgsConstructor
public class SnippetController {

    private final SnippetService snippetService;
    private final DiffService diffService;
    private final CompilerService compilerService;

    @PostMapping
    public ResponseEntity<SnippetResponse> createSnippet(@RequestBody SnippetRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(snippetService.createSnippet(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SnippetResponse> updateSnippet(@PathVariable Long id, @RequestBody SnippetRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(snippetService.updateSnippet(id, request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<SnippetResponse>> getAllSnippets(Authentication authentication) {
        return ResponseEntity.ok(snippetService.getAllSnippets(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> getSnippet(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(snippetService.getSnippet(id, authentication.getName()));
    }

    @PostMapping("/validate")
    public ResponseEntity<List<String>> validateSnippet(@RequestBody SnippetRequest request) {
        return ResponseEntity.ok(compilerService.validateSyntax(request.getContent()));
    }

    @GetMapping("/{id}/diff")
    public ResponseEntity<DiffService.DiffResult> getDiff(@PathVariable Long id, @RequestParam int v1,
            @RequestParam int v2, Authentication authentication) {
        String content1 = snippetService.getVersionContent(id, v1, authentication.getName());
        String content2 = snippetService.getVersionContent(id, v2, authentication.getName());
        return ResponseEntity.ok(diffService.compare(content1, content2));
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<SnippetResponse> rollback(@PathVariable Long id, @RequestBody RollbackRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(snippetService.rollback(id, request.getVersionNumber(), authentication.getName()));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<com.enterprise.codeplatform.dto.VersionResponse>> getVersions(@PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(snippetService.getSnippetVersions(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id, @PathVariable int versionNumber,
            Authentication authentication) {
        snippetService.deleteVersion(id, versionNumber, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(@PathVariable Long id, Authentication authentication) {
        snippetService.deleteSnippet(id, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
