package com.enterprise.codeplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CodeAnalysisPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeAnalysisPlatformApplication.class, args);
    }
}
