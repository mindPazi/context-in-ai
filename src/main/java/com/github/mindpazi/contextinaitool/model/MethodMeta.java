package com.github.mindpazi.contextinaitool.model;

public record MethodMeta(
                String classFqn, // e.g. com.example.Foo
                String methodName,
                String filePath) {
}
