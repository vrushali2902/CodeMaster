package com.enterprise.codeplatform.service;

import com.enterprise.codeplatform.entity.CodeMetrics;
import com.enterprise.codeplatform.entity.CodeVersion;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class MetricsService {

    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"));

    public CodeMetrics calculateMetrics(CodeVersion version) {
        String content = version.getContent();
        int loc = (int) content.lines().count();
        int keywordCount = countKeywords(content);
        int complexity = estimateComplexity(content);

        return CodeMetrics.builder()
                .version(version)
                .loc(loc)
                .keywordCount(keywordCount)
                .cyclomaticComplexity(complexity)
                .build();
    }

    private int countKeywords(String content) {
        String[] words = content.split("\\W+");
        int count = 0;
        for (String word : words) {
            if (JAVA_KEYWORDS.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private int estimateComplexity(String content) {
        // Simplified Cyclomatic Complexity: count control flow statements
        String[] controlFlow = { "if", "for", "while", "case", "&&", "||", "catch" };
        int count = 1; // Base complexity
        for (String word : content.split("\\W+")) {
            for (String ctrl : controlFlow) {
                if (word.equals(ctrl))
                    count++;
            }
        }
        return count;
    }
}
