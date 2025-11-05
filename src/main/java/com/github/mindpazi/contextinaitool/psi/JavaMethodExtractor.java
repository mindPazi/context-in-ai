package com.github.mindpazi.contextinaitool.psi;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

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

            PsiClass containingClass = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class, true);
            if (containingClass != null && containingClass.getQualifiedName() != null) {
                className = containingClass.getQualifiedName() + "$"
                        + Integer.toHexString(System.identityHashCode(psiClass));
                LOG.debug("Using synthetic name for anonymous class: " + className);
            } else {
                LOG.debug("Skipping class with null qualified name and no valid containing class");
                return;
            }
        }

        LOG.debug("Processing class: " + className);

        int methodCount = 0;

        for (PsiElement child : psiClass.getChildren()) {
            if (child instanceof PsiMethod method) {
                String methodName = method.getName();
                LOG.debug("Processing method: " + methodName);

                String signature = getMethodSignature(method);
                String body = getMethodBody(method);

                MethodMeta meta = new MethodMeta(
                        className,
                        methodName,
                        signature,
                        filePath,
                        body);
                out.add(meta);
                methodCount++;

                extractMethodsFromAnonymousClasses(method, filePath, out);
                extractMethodsFromLambdas(method, filePath, out);
            }
        }

        PsiClass[] innerClasses = psiClass.getInnerClasses();
        for (PsiClass innerClass : innerClasses) {
            LOG.debug("Found inner class: " + innerClass.getName());
            extractMethodsFromClass(innerClass, filePath, out);
        }

        LOG.debug("Found " + methodCount + " methods in class " + className);
    }

    private static void extractMethodsFromAnonymousClasses(PsiMethod method, String filePath, Set<MethodMeta> out) {
        if (method.getBody() == null) {
            return;
        }

        method.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitAnonymousClass(PsiAnonymousClass aClass) {
                super.visitAnonymousClass(aClass);
                extractMethodsFromClass(aClass, filePath, out);
            }
        });
    }

    private static void extractMethodsFromLambdas(PsiMethod method, String filePath, Set<MethodMeta> out) {
        if (method.getBody() == null) {
            return;
        }

        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return;
        }

        String containingClassName = containingClass.getQualifiedName();
        if (containingClassName == null) {

            containingClassName = containingClass.getName();
            if (containingClassName == null) {
                containingClassName = "UnknownClass";
            }
        }

        final String lambdaClassName = containingClassName;

        method.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitLambdaExpression(PsiLambdaExpression lambda) {
                super.visitLambdaExpression(lambda);

                String lambdaMethodName = "lambda$" + method.getName();
                String signature = getLambdaSignature(lambda);
                String body = getLambdaBody(lambda);

                MethodMeta meta = new MethodMeta(
                        lambdaClassName,
                        lambdaMethodName,
                        signature,
                        filePath,
                        body);
                out.add(meta);
                LOG.debug("Found lambda in method: " + method.getName());
            }
        });
    }

    private static String getLambdaSignature(PsiLambdaExpression lambda) {
        PsiParameterList paramList = lambda.getParameterList();
        PsiParameter[] parameters = paramList.getParameters();

        if (parameters.length == 0) {
            return "()";
        }

        boolean isDumb = lambda.getProject() != null && DumbService.isDumb(lambda.getProject());

        return Arrays.stream(parameters)
                .map(p -> {
                    PsiTypeElement typeElement = p.getTypeElement();
                    if (typeElement != null) {

                        return typeElement.getText();
                    }

                    if (!isDumb) {
                        try {
                            PsiType type = p.getType();
                            return type.getPresentableText();
                        } catch (Exception e) {
                            LOG.debug("Failed to resolve lambda parameter type: " + e.getMessage());
                        }
                    }

                    return "?";
                })
                .collect(Collectors.joining(", ", "(", ")"));
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

    private static String getMethodBody(PsiMethod method) {
        PsiCodeBlock body = method.getBody();
        return body != null ? body.getText() : "";
    }

    private static String getLambdaBody(PsiLambdaExpression lambda) {
        PsiElement body = lambda.getBody();
        return body != null ? body.getText() : "";
    }
}
