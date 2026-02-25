package com.enterprise.codeplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "code_metrics")
public class CodeMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "version_id", nullable = false)
    private CodeVersion version;

    private int loc;
    private int cyclomaticComplexity;
    private int keywordCount;
}
