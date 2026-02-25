package com.enterprise.codeplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "code_snippets")
public class CodeSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String currentContent;

    @Column(nullable = false)
    private String language;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    @OneToMany(mappedBy = "snippet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeVersion> versions;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private int activeVersionNumber;
}
