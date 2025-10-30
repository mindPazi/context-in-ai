package com.github.mindpazi.contextinaitool.action;

import com.github.mindpazi.contextinaitool.activity.ProjectStartup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DumpMethods extends AnAction {
    private static final Logger LOG = Logger.getInstance(ProjectStartup.class);

    public DumpMethods() {
        super("Dump Methods to JSON", "Write methods.json for the project", (Icon) null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LOG.warn("✅ DumpMethods triggered!");
    }
}
