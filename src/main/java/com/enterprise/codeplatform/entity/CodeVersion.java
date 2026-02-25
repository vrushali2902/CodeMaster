package com.enterprise.codeplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "code_versions")
public class CodeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    private CodeSnippet snippet;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int versionNumber;

    private String commitMessage;

    @CreatedDate
    private LocalDateTime createdAt;
}
