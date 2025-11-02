package com.github.mindpazi.contextinaitool.model;

import java.io.Serializable;
import java.util.Set;

public record MethodsPerFileValue(
        Set<MethodMeta> methods
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
