package com.github.mindpazi.contextinaitool.model;

public record MethodMeta(
        String classFqn,   // e.g. com.example.Foo
        String methodName,
        String filePath,
        int startLine,     // 1-based
        int endLine        // 1-based
) {}
