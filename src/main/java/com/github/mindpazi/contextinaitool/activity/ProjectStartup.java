package com.github.mindpazi.contextinaitool.activity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class ProjectStartup implements StartupActivity.DumbAware {
    private static final Logger LOG = Logger.getInstance(ProjectStartup.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.warn("✅ ProjectStartup called for project: " + project.getName());
        System.out.println("✅ [STDOUT] ProjectStartup for project: " + project.getName());
    }
}
