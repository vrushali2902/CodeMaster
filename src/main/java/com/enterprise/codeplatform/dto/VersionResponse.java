package com.enterprise.codeplatform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VersionResponse {
    private Long id;
    private int versionNumber;
    private String content;
    private String commitMessage;
    private LocalDateTime createdAt;
}
