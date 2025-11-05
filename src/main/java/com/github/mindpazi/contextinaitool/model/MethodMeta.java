package com.github.mindpazi.contextinaitool.model;

import java.io.Serializable;

public record MethodMeta(
                String classFqn,
                String methodName,
                String signature,
                String filePath,
                String body) implements Serializable {
}
