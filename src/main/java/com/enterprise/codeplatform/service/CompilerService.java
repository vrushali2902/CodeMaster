package com.enterprise.codeplatform.service;

import org.springframework.stereotype.Service;

import javax.tools.*;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompilerService {

    public List<String> validateSyntax(String code) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            return Collections.singletonList(
                    "Java Compiler not available. Make sure you are running with JDK, not JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaFileObject file = new JavaSourceFromString("Test", code);

        List<String> options = List.of("-proc:none"); // Disable annotation processing for speed and to avoid warnings

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                null,
                diagnostics,
                options,
                null,
                Collections.singletonList(file));

        task.call();

        return diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> "Line " + d.getLineNumber() + ": " + d.getMessage(null))
                .collect(Collectors.toList());
    }

    // Inner class to hold source code in memory
    static class JavaSourceFromString extends SimpleJavaFileObject {

        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}