package com.github.mindpazi.contextinaitool.psi;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

public final class JavaMethodExtractor {

    private static final Logger LOG = Logger.getInstance(JavaMethodExtractor.class);

    private JavaMethodExtractor() {
    }

    public static List<MethodMeta> extract(PsiJavaFile psiJavaFile) {
        VirtualFile vf = psiJavaFile.getVirtualFile();
        String filePath = vf != null ? vf.getPath() : psiJavaFile.getName();
        LOG.debug("Extracting methods from: " + filePath);

        List<MethodMeta> out = new ArrayList<>();

        Project project = psiJavaFile.getProject();
        Document doc = PsiDocumentManager.getInstance(project).getDocument(psiJavaFile);

        PsiClass[] classes = psiJavaFile.getClasses();
        LOG.debug("Found " + classes.length + " top-level classes in file");

        for (PsiClass psiClass : classes) {
            extractMethodsFromClass(psiClass, filePath, doc, out);
        }

        LOG.debug("Total methods extracted: " + out.size());
        return out;
    }

    private static void extractMethodsFromClass(PsiClass psiClass, String filePath, Document doc,
            List<MethodMeta> out) {
        String className = psiClass.getQualifiedName();
        if (className == null) {
            className = psiClass.getName();
        }
        LOG.debug("Processing class: " + className);

        PsiElement[] children = psiClass.getChildren();
        int methodCount = 0;

        for (PsiElement child : children) {
            if (child instanceof PsiMethod method) {
                methodCount++;
                String methodName = method.getName();
                LOG.debug("Processing method: " + methodName);

                out.add(new MethodMeta(
                        className,
                        methodName,
                        filePath));
            } else if (child instanceof PsiClass innerClass) {
                LOG.debug("Found inner class: " + innerClass.getName());
                extractMethodsFromClass(innerClass, filePath, doc, out);
            }
        }

        LOG.debug("Found " + methodCount + " methods in class " + className);
    }
}
