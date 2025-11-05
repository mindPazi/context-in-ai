package com.github.mindpazi.contextinaitool.psi;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.*;

import java.util.*;
import java.util.stream.Collectors;

public class JavaMethodExtractor {

    private static final Logger LOG = Logger.getInstance(JavaMethodExtractor.class);

    public static Set<MethodMeta> extract(PsiJavaFile javaFile) {
        if (javaFile == null) {
            LOG.debug("Cannot extract methods: javaFile is null");
            return Collections.emptySet();
        }

        String filePath = javaFile.getVirtualFile().getPath();
        LOG.debug("Extracting methods from: " + filePath);

        Set<MethodMeta> out = new LinkedHashSet<>();

        PsiClass[] classes = javaFile.getClasses();
        LOG.debug("Found " + classes.length + " top-level classes in file");

        for (PsiClass topClass : classes) {
            extractMethodsFromClass(topClass, filePath, out);
        }

        LOG.debug("Total methods extracted: " + out.size());
        return out;
    }

    private static void extractMethodsFromClass(PsiClass psiClass, String filePath, Set<MethodMeta> out) {
        if (psiClass == null) {
            return;
        }

        String className = psiClass.getQualifiedName();
        if (className == null) {
            LOG.debug("Skipping class with null qualified name");
            return;
        }

        LOG.debug("Processing class: " + className);

        int methodCount = 0;

        for (PsiElement child : psiClass.getChildren()) {
            if (child instanceof PsiMethod method) {
                String methodName = method.getName();
                LOG.debug("Processing method: " + methodName);

                String signature = getMethodSignature(method);

                MethodMeta meta = new MethodMeta(
                        className,
                        methodName,
                        signature,
                        filePath);
                out.add(meta);
                methodCount++;
            }
        }

        PsiClass[] innerClasses = psiClass.getInnerClasses();
        for (PsiClass innerClass : innerClasses) {
            LOG.debug("Found inner class: " + innerClass.getName());
            extractMethodsFromClass(innerClass, filePath, out);
        }

        LOG.debug("Found " + methodCount + " methods in class " + className);
    }

    private static String getMethodSignature(PsiMethod method) {
        PsiParameterList paramList = method.getParameterList();
        PsiParameter[] parameters = paramList.getParameters();

        if (parameters.length == 0) {
            return "()";
        }

        boolean isDumb = method.getProject() != null && DumbService.isDumb(method.getProject());

        return Arrays.stream(parameters)
                .map(p -> getParameterTypeText(p, isDumb))
                .collect(Collectors.joining(", ", "(", ")"));
    }

    private static String getParameterTypeText(PsiParameter parameter, boolean isDumb) {
        if (!isDumb) {
            try {
                return parameter.getType().getPresentableText();
            } catch (Exception e) {
                LOG.debug("Failed to get type from index, falling back to text: " + e.getMessage());
            }
        }

        PsiTypeElement typeElement = parameter.getTypeElement();
        if (typeElement != null) {
            return typeElement.getText();
        }

        return "Object";
    }
}
