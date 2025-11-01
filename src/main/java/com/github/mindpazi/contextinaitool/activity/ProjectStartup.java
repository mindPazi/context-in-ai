package com.github.mindpazi.contextinaitool.activity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class ProjectStartup implements StartupActivity.DumbAware {
    private static final Logger LOG = Logger.getInstance(ProjectStartup.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.debug("√ ProjectStartup called for project: " + project.getName());

        DumbService.getInstance(project).runWhenSmart(() -> {
            LOG.debug("√ Index ready for project: " + project.getName());

        });
    }
}
