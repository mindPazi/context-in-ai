package com.github.mindpazi.contextinaitool.index;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import org.junit.Test;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class MethodsPerFileExternalizerTest {

    @Test
    public void testSaveAndRead() throws IOException {
        MethodsPerFileExternalizer externalizer = MethodsPerFileExternalizer.INSTANCE;

        Set<MethodMeta> originalMethods = new LinkedHashSet<>();
        originalMethods.add(new MethodMeta("com.example.TestClass", "method1", "()", "/path/to/file.java", ""));
        originalMethods.add(new MethodMeta("com.example.TestClass", "method2", "(String)", "/path/to/file.java", ""));
        originalMethods.add(new MethodMeta("com.example.OtherClass", "method3", "(int, int)", "/path/to/other.java", ""));

        MethodsPerFileValue originalValue = new MethodsPerFileValue(originalMethods);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        externalizer.save(out, originalValue);

        byte[] data = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        MethodsPerFileValue readValue = externalizer.read(in);

        Set<MethodMeta> readMethods = readValue.methods();
        assertEquals(originalMethods.size(), readMethods.size());

        for (MethodMeta original : originalMethods) {
            boolean found = readMethods.stream().anyMatch(read ->
                    original.classFqn().equals(read.classFqn()) &&
                    original.methodName().equals(read.methodName()) &&
                    original.signature().equals(read.signature()) &&
                    original.filePath().equals(read.filePath())
            );
            assertEquals("Should find method: " + original.methodName(), true, found);
        }
    }

    @Test
    public void testEmptyList() throws IOException {
        MethodsPerFileExternalizer externalizer = MethodsPerFileExternalizer.INSTANCE;

        Set<MethodMeta> emptySet = new LinkedHashSet<>();
        MethodsPerFileValue originalValue = new MethodsPerFileValue(emptySet);

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
