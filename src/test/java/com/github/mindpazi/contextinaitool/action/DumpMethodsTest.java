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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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

        Set<MethodMeta> extractedMethods = JavaMethodExtractor.extract(javaFile);

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
        Set<MethodMeta> testMethods = new LinkedHashSet<>();
        testMethods.add(new MethodMeta("com.test.ClassA", "methodA", "()", "/test/ClassA.java"));
        testMethods.add(new MethodMeta("com.test.ClassB", "methodB", "(String)", "/test/ClassB.java"));
        testMethods.add(new MethodMeta("com.test.ClassB", "methodC", "(int, int)", "/test/ClassB.java"));

        assertEquals(3, testMethods.size());
        assertTrue(testMethods.stream().anyMatch(m -> m.methodName().equals("methodA") && m.classFqn().equals("com.test.ClassA")));
    }

    public void testMethodsPerFileValue() {
        Set<MethodMeta> methods = new LinkedHashSet<>();
        methods.add(new MethodMeta("com.example.Test", "test1", "()", "/path/Test.java"));
        methods.add(new MethodMeta("com.example.Test", "test2", "(String)", "/path/Test.java"));

        MethodsPerFileValue value = new MethodsPerFileValue(methods);

        assertNotNull(value);
        assertNotNull(value.methods());
        assertEquals(2, value.methods().size());
        assertTrue(value.methods().stream().anyMatch(m -> m.methodName().equals("test1")));
        assertTrue(value.methods().stream().anyMatch(m -> m.methodName().equals("test2")));
    }

    public void testEmptyMethodsList() {
        Set<MethodMeta> emptySet = Set.of();
        MethodsPerFileValue value = new MethodsPerFileValue(emptySet);

        assertNotNull(value);
        assertNotNull(value.methods());
        assertEquals(0, value.methods().size());
    }
}
