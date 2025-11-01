package com.github.mindpazi.contextinaitool.index;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import com.github.mindpazi.contextinaitool.psi.JavaMethodExtractor;
import com.intellij.openapi.diagnostic.Logger;
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

    private static final Logger LOG = Logger.getInstance(MethodsPerFileIndex.class);

    public static final ID<String, MethodsPerFileValue> INDEX_ID = ID
            .create("com.github.mindpazi.contextinaitool.methodsPerFile");

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
                String filePath = inputData.getFile().getPath();
                LOG.debug("Indexing file: " + filePath);

                PsiFile psiFile = inputData.getPsiFile();
                if (!(psiFile instanceof PsiJavaFile javaFile)) {
                    LOG.debug("Not a Java file, skipping: " + filePath);
                    return Collections.emptyMap();
                }

                List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
                LOG.debug("Extracted " + methods.size() + " methods from: " + filePath);

                if (methods.isEmpty()) {
                    LOG.debug("No methods found in: " + filePath);
                    return Collections.emptyMap();
                }

                LOG.debug("Storing " + methods.size() + " methods with key: " + filePath);
                return Map.of(filePath, new MethodsPerFileValue(methods));
            }
        };
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
        return 3;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull com.intellij.openapi.vfs.VirtualFile file) {
                boolean isJava = "java".equals(file.getExtension());
                if (isJava) {
                    LOG.debug("Accepting Java file for indexing: " + file.getPath());
                }
                return isJava;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
