package com.github.mindpazi.contextinaitool.action;

import com.github.mindpazi.contextinaitool.dump.JsonDumper;
import com.github.mindpazi.contextinaitool.index.MethodsPerFileIndex;
import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class DumpMethods extends AnAction {
    private static final Logger LOG = Logger.getInstance(DumpMethods.class);

    public DumpMethods() {
        super("Dump Methods to JSON", "Write methods.json for the project", (Icon) null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        if (DumbService.getInstance(project).isDumb()) {
            Notifications.Bus.notify(new Notification(
                    "MethodDumper",
                    "Indexing in Progress",
                    "Please wait for the project indexing to complete before dumping methods.",
                    NotificationType.INFORMATION), project);
        }

        DumbService.getInstance(project).runWhenSmart(() -> {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    Set<MethodMeta> allMethods = ReadAction.compute(() -> collectAllMethods(project));
                    JsonDumper.dump(allMethods, project);
                    LOG.debug("√ Dumped " + allMethods.size() + " methods to JSON");

                    ApplicationManager.getApplication().invokeLater(() -> {
                        Notifications.Bus.notify(new Notification(
                                "MethodDumper",
                                "Methods Dumped Successfully",
                                "Dumped " + allMethods.size() + " methods to methods.json",
                                NotificationType.INFORMATION), project);
                    });
                } catch (Exception ex) {
                    LOG.error("Failed to dump methods", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Notifications.Bus.notify(new Notification(
                                "MethodDumper",
                                "Dump Failed",
                                "Failed to dump methods: " + ex.getMessage(),
                                NotificationType.ERROR), project);
                    });
                }
            });
        });
    }

    private Set<MethodMeta> collectAllMethods(Project project) {
        Set<MethodMeta> allMethods = new LinkedHashSet<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<String> allKeys = index.getAllKeys(MethodsPerFileIndex.INDEX_ID, project);

        LOG.debug("Found " + allKeys.size() + " keys in index");
        for (String key : allKeys) {
            LOG.debug("Processing key: " + key);
            index.processValues(
                    MethodsPerFileIndex.INDEX_ID,
                    key,
                    null,
                    (file, value) -> {
                        if (value != null && value.methods() != null) {
                            LOG.debug("Found " + value.methods().size() + " methods in file: " + file.getPath());
                            allMethods.addAll(value.methods());
                        }
                        return true;
                    },
                    scope);
        }

        return allMethods;
    }
}
