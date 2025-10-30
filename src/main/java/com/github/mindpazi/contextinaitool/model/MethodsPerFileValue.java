package com.github.mindpazi.contextinaitool.model;

import java.io.Serializable;
import java.util.List;

public record MethodsPerFileValue(
        List<MethodMeta> methods
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
