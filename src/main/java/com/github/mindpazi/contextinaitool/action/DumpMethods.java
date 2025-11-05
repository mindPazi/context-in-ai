package com.github.mindpazi.contextinaitool.action;

import com.github.mindpazi.contextinaitool.dump.JsonDumper;
import com.github.mindpazi.contextinaitool.model.ExtractionStats;
import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.psi.JavaMethodExtractor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
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
            return;
        }

        DumbService.getInstance(project).runWhenSmart(() -> {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    ExtractionResult result = ReadAction.compute(() -> collectAllMethodsWithStats(project));
                    Set<MethodMeta> allMethods = result.methods;
                    ExtractionStats stats = result.stats;

                    JsonDumper.dump(allMethods, project);

                    project.getBaseDir().refresh(false, true);

                    LOG.debug("√ Dumped " + allMethods.size() + " methods to JSON");

                    ApplicationManager.getApplication().invokeLater(() -> {
                        String message = String.format(
                                "Methods: %d, Anonymous Classes: %d, Lambdas: %d (Total: %d)",
                                stats.getMethodCount(),
                                stats.getAnonymousClassCount(),
                                stats.getLambdaCount(),
                                stats.getTotalCount());

                        Notifications.Bus.notify(new Notification(
                                "MethodDumper",
                                "Methods Dumped Successfully",
                                message,
                                NotificationType.INFORMATION), project);
                    });
                } catch (ProcessCanceledException ex) {
                    LOG.info("Method dump operation was cancelled");
                    throw ex;
                } catch (IOException ex) {
                    LOG.error("Failed to write methods.json file", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Unknown IO error";
                        Notifications.Bus.notify(new Notification(
                                "MethodDumper",
                                "File Write Error",
                                "Cannot write methods.json: " + errorMsg +
                                        ". Check file permissions and disk space.",
                                NotificationType.ERROR), project);
                    });
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    LOG.error("Invalid project state or arguments", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        String errorMsg = ex.getMessage() != null ? ex.getMessage() : "Invalid state";
                        Notifications.Bus.notify(new Notification(
                                "MethodDumper",
                                "Invalid Project State",
                                errorMsg,
                                NotificationType.ERROR), project);
                    });
                } catch (Exception ex) {
                    LOG.error("Unexpected error during method extraction", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Notifications.Bus.notify(new Notification(
                                "MethodDumper",
                                "Unexpected Error",
                                "An unexpected error occurred. See IDE logs for details.",
                                NotificationType.ERROR), project);
                    });
                }
            });
        });
    }

    private static class ExtractionResult {
        final Set<MethodMeta> methods;
        final ExtractionStats stats;

        ExtractionResult(Set<MethodMeta> methods, ExtractionStats stats) {
            this.methods = methods;
            this.stats = stats;
        }
    }

    private ExtractionResult collectAllMethodsWithStats(Project project) {
        Set<MethodMeta> allMethods = new LinkedHashSet<>();
        ExtractionStats totalStats = new ExtractionStats();
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        PsiManager psiManager = PsiManager.getInstance(project);

        LOG.debug("Scanning project for Java files...");

        fileIndex.iterateContent(file -> {
            if (file.getExtension() != null && file.getExtension().equals("java")) {
                VirtualFile virtualFile = file;
                var psiFile = psiManager.findFile(virtualFile);

                if (psiFile instanceof PsiJavaFile javaFile) {
                    LOG.debug("Extracting methods from: " + file.getPath());
                    JavaMethodExtractor.ExtractionResult result = JavaMethodExtractor.extractWithStats(javaFile);
                    allMethods.addAll(result.getMethods());

                    ExtractionStats fileStats = result.getStats();
                    totalStats.merge(fileStats);
                }
            }
            return true;
        });

        LOG.debug("Total methods collected: " + allMethods.size());
        return new ExtractionResult(allMethods, totalStats);
    }
}
