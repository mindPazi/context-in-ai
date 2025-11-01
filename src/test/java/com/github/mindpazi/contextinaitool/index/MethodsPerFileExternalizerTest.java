package com.github.mindpazi.contextinaitool.index;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MethodsPerFileExternalizerTest {

    @Test
    public void testSaveAndRead() throws IOException {
        MethodsPerFileExternalizer externalizer = MethodsPerFileExternalizer.INSTANCE;

        List<MethodMeta> originalMethods = Arrays.asList(
                new MethodMeta("com.example.TestClass", "method1", "/path/to/file.java"),
                new MethodMeta("com.example.TestClass", "method2", "/path/to/file.java"),
                new MethodMeta("com.example.OtherClass", "method3", "/path/to/other.java"));

        MethodsPerFileValue originalValue = new MethodsPerFileValue(originalMethods);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        externalizer.save(out, originalValue);

        byte[] data = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        MethodsPerFileValue readValue = externalizer.read(in);

        List<MethodMeta> readMethods = readValue.methods();
        assertEquals(originalMethods.size(), readMethods.size());

        for (int i = 0; i < originalMethods.size(); i++) {
            MethodMeta original = originalMethods.get(i);
            MethodMeta read = readMethods.get(i);

            assertEquals(original.classFqn(), read.classFqn());
            assertEquals(original.methodName(), read.methodName());
            assertEquals(original.filePath(), read.filePath());
        }
    }

    @Test
    public void testEmptyList() throws IOException {
        MethodsPerFileExternalizer externalizer = MethodsPerFileExternalizer.INSTANCE;

        List<MethodMeta> emptyList = Arrays.asList();
        MethodsPerFileValue originalValue = new MethodsPerFileValue(emptyList);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        externalizer.save(out, originalValue);

        byte[] data = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        MethodsPerFileValue readValue = externalizer.read(in);

        assertEquals(0, readValue.methods().size());
    }
}
