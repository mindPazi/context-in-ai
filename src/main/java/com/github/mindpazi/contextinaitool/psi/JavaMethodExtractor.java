package com.github.mindpazi.contextinaitool.psi;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

public final class JavaMethodExtractor {

    private JavaMethodExtractor() {}

    public static List<MethodMeta> extract(PsiJavaFile psiJavaFile) {
        Project project = psiJavaFile.getProject();
        Document doc = PsiDocumentManager.getInstance(project).getDocument(psiJavaFile);
        if (doc == null) return List.of();

        VirtualFile vf = psiJavaFile.getVirtualFile();
        String filePath = vf != null ? vf.getPath() : psiJavaFile.getName();

        List<MethodMeta> out = new ArrayList<>();

        for (PsiMethod method : PsiTreeUtil.findChildrenOfType(psiJavaFile, PsiMethod.class)) {
            PsiClass containingClass = method.getContainingClass();
            String fqn = containingClass != null
                    ? containingClass.getQualifiedName()
                    : "<anonymous>";

            int startLine = doc.getLineNumber(method.getTextRange().getStartOffset()) + 1;
            int endLine = doc.getLineNumber(Math.max(0, method.getTextRange().getEndOffset() - 1)) + 1;

            out.add(new MethodMeta(
                    fqn,
                    method.getName(),
                    filePath,
                    startLine,
                    endLine
            ));
        }

        return out;
    }
}
