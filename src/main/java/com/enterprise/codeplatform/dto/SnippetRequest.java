package com.enterprise.codeplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnippetRequest {
    private String title;
    private String description;
    private String content;
    private String language;
}
