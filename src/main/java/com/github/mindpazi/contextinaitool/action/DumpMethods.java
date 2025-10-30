package com.github.mindpazi.contextinaitool.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DumpMethods extends AnAction {

    public DumpMethods() {
        super("Dump Methods to JSON", "Write methods.json for the project", (Icon) null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("✅ DumpMethods triggered!");
    }
}
