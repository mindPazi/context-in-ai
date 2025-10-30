package com.github.mindpazi.contextinaitool.index;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import com.github.mindpazi.contextinaitool.psi.JavaMethodExtractor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MethodsPerFileIndex extends FileBasedIndexExtension<String, MethodsPerFileValue> {
    
    public static final ID<String, MethodsPerFileValue> INDEX_ID = 
            ID.create("com.github.mindpazi.contextinaitool.methodsPerFile");
    
    private static final String KEY = "methods";
    
    @NotNull
    @Override
    public ID<String, MethodsPerFileValue> getName() {
        return INDEX_ID;
    }
    
    @NotNull
    @Override
    public DataIndexer<String, MethodsPerFileValue, FileContent> getIndexer() {
        return new DataIndexer<>() {
            @NotNull
            @Override
            public Map<String, MethodsPerFileValue> map(@NotNull FileContent inputData) {
                PsiFile psiFile = inputData.getPsiFile();
                if (!(psiFile instanceof PsiJavaFile javaFile)) {
                    return Collections.emptyMap();
                }

                List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
                if (methods.isEmpty()) {
                    return Collections.emptyMap();
                }

                return Map.of(KEY, new MethodsPerFileValue(methods));
            }
        };
        // Lambda version:
        // return inputData -> {
        //     PsiFile psiFile = inputData.getPsiFile();
        //     if (!(psiFile instanceof PsiJavaFile javaFile)) {
        //         return Collections.emptyMap();
        //     }
        //     
        //     List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
        //     if (methods.isEmpty()) {
        //         return Collections.emptyMap();
        //     }
        //     
        //     return Map.of(KEY, new MethodsPerFileValue(methods));
        // };
    }
    
    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }
    
    @NotNull
    @Override
    public DataExternalizer<MethodsPerFileValue> getValueExternalizer() {
        return MethodsPerFileExternalizer.INSTANCE;
    }
    
    @Override
    public int getVersion() {
        return 1;
    }
    
    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull com.intellij.openapi.vfs.VirtualFile file) {
                return "java".equals(file.getExtension());
            }
        };
        // Lambda version: return file -> "java".equals(file.getExtension());
    }
    
    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
