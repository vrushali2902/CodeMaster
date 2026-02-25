package com.enterprise.codeplatform.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiffService {

    @Data
    @Builder
    public static class DiffResult {
        private String original;
        private String revised;
        private List<String> deltas;
    }

    public DiffResult compare(String originalContent, String revisedContent) {
        List<String> originalLines = Arrays.asList(originalContent.split("\n"));
        List<String> revisedLines = Arrays.asList(revisedContent.split("\n"));

        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);

        List<String> deltas = patch.getDeltas().stream()
                .map(AbstractDelta::toString)
                .collect(Collectors.toList());

        return DiffResult.builder()
                .original(originalContent)
                .revised(revisedContent)
                .deltas(deltas)
                .build();
    }
}
