package com.github.mindpazi.contextinaitool.index;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MethodsPerFileExternalizer implements DataExternalizer<MethodsPerFileValue> {

    public static final MethodsPerFileExternalizer INSTANCE = new MethodsPerFileExternalizer();

    private MethodsPerFileExternalizer() {
    }

    @Override
    public void save(@NotNull DataOutput out, MethodsPerFileValue value) throws IOException {
        List<MethodMeta> methods = value.methods();
        out.writeInt(methods.size());

        for (MethodMeta method : methods) {
            IOUtil.writeUTF(out, method.classFqn());
            IOUtil.writeUTF(out, method.methodName());
            IOUtil.writeUTF(out, method.filePath());
        }
    }

    @Override
    public MethodsPerFileValue read(@NotNull DataInput in) throws IOException {
        int size = in.readInt();
        List<MethodMeta> methods = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            String classFqn = IOUtil.readUTF(in);
            String methodName = IOUtil.readUTF(in);
            String filePath = IOUtil.readUTF(in);

            methods.add(new MethodMeta(classFqn, methodName, filePath));
        }

        return new MethodsPerFileValue(methods);
    }
}
