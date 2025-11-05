package com.github.mindpazi.contextinaitool.dump;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class JsonDumper {
    public static void dump(Set<MethodMeta> methods, Project project) throws IOException {
        if (methods == null) {
            throw new IllegalArgumentException("Methods collection cannot be null");
        }

        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new IllegalStateException("Project base path is null. Cannot determine output location.");
        }

        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        String json = gson.toJson(methods);
        Path outputPath = Paths.get(basePath, "methods.json");
        Files.writeString(outputPath, json);
    }
}
