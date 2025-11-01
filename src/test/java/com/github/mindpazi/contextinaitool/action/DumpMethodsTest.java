package com.github.mindpazi.contextinaitool.action;

import com.github.mindpazi.contextinaitool.index.MethodsPerFileIndex;
import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.github.mindpazi.contextinaitool.model.MethodsPerFileValue;
import com.github.mindpazi.contextinaitool.psi.JavaMethodExtractor;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DumpMethodsTest extends BasePlatformTestCase {

    public void testJavaMethodExtraction() {
        String javaCode = """
                package com.example;

                public class TestClass {
                    public void publicMethod() {
                        System.out.println("Public");
                    }

                    private String privateMethod(String param) {
                        return "Result: " + param;
                    }

                    protected int protectedMethod(int x) {
                        return x * 2;
                    }
                }
                """;

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java", javaCode);

        List<MethodMeta> extractedMethods = JavaMethodExtractor.extract(javaFile);

        assertNotNull(extractedMethods);
        assertEquals(3, extractedMethods.size());

        assertTrue("Should extract publicMethod",
                extractedMethods.stream().anyMatch(m -> m.methodName().equals("publicMethod")));
        assertTrue("Should extract privateMethod",
                extractedMethods.stream().anyMatch(m -> m.methodName().equals("privateMethod")));
        assertTrue("Should extract protectedMethod",
                extractedMethods.stream().anyMatch(m -> m.methodName().equals("protectedMethod")));

        assertTrue("All methods should have correct classFqn",
                extractedMethods.stream().allMatch(m -> m.classFqn().equals("com.example.TestClass")));
    }

    public void testMethodStructure() {
        List<MethodMeta> testMethods = Arrays.asList(
                new MethodMeta("com.test.ClassA", "methodA", "/test/ClassA.java"),
                new MethodMeta("com.test.ClassB", "methodB", "/test/ClassB.java"),
                new MethodMeta("com.test.ClassB", "methodC", "/test/ClassB.java"));

        assertEquals(3, testMethods.size());
        assertEquals("methodA", testMethods.get(0).methodName());
        assertEquals("com.test.ClassA", testMethods.get(0).classFqn());
    }

    public void testMethodsPerFileValue() {
        List<MethodMeta> methods = Arrays.asList(
                new MethodMeta("com.example.Test", "test1", "/path/Test.java"),
                new MethodMeta("com.example.Test", "test2", "/path/Test.java"));

        MethodsPerFileValue value = new MethodsPerFileValue(methods);

        assertNotNull(value);
        assertNotNull(value.methods());
        assertEquals(2, value.methods().size());
        assertEquals("test1", value.methods().get(0).methodName());
        assertEquals("test2", value.methods().get(1).methodName());
    }

    public void testEmptyMethodsList() {
        List<MethodMeta> emptyList = List.of();
        MethodsPerFileValue value = new MethodsPerFileValue(emptyList);

        assertNotNull(value);
        assertNotNull(value.methods());
        assertEquals(0, value.methods().size());
    }
}
