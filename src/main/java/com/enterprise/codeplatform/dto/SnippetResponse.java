package com.enterprise.codeplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnippetResponse {
    private Long id;
    private String title;
    private String description;
    private String currentContent;
    private String language;
    private String authorName;
    private int activeVersionNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
