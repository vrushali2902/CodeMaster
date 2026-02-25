package com.enterprise.codeplatform.controller;

import com.enterprise.codeplatform.service.SnippetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionController {

    private final SnippetService snippetService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id, Authentication authentication) {
        snippetService.deleteVersionById(id, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
