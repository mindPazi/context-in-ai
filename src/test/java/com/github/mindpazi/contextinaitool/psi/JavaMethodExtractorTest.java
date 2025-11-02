package com.github.mindpazi.contextinaitool.psi;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Set;

public class JavaMethodExtractorTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/extractor";
    }

    public void testExtractSimpleMethod() {
        String javaCode = """
                package com.example;

                public class TestClass {
                    public void simpleMethod() {
                        System.out.println("Hello");
                    }
                }
                """;

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java", javaCode);

        Set<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);

        assertEquals(1, methods.size());

        MethodMeta method = methods.iterator().next();
        assertEquals("com.example.TestClass", method.classFqn());
        assertEquals("simpleMethod", method.methodName());
        assertEquals("()", method.signature());
    }

    public void testExtractMultipleMethods() {
        String javaCode = """
                 package com.example;
                \s
                 public class TestClass {
                     public void firstMethod() {
                         System.out.println("First");
                     }
                     \s
                     private String secondMethod(String param) {
                         return "Second: " + param;
                     }
                    \s
                     protected int thirdMethod() {
                         return 42;
                     }
                 }
                \s""";

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java", javaCode);

        Set<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);

        assertEquals(3, methods.size());

        MethodMeta first = methods.stream()
                .filter(m -> m.methodName().equals("firstMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(first);
        assertEquals("com.example.TestClass", first.classFqn());
        assertEquals("()", first.signature());

        MethodMeta second = methods.stream()
                .filter(m -> m.methodName().equals("secondMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(second);
        assertEquals("com.example.TestClass", second.classFqn());
        assertEquals("(String)", second.signature());

        MethodMeta third = methods.stream()
                .filter(m -> m.methodName().equals("thirdMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(third);
        assertEquals("com.example.TestClass", third.classFqn());
        assertEquals("()", third.signature());
    }

    public void testExtractConstructor() {
        String javaCode = """
                 package com.example;
                \s
                 public class TestClass {
                     public TestClass() {
                         System.out.println("Constructor");
                     }
                    \s
                     public TestClass(String param) {
                         System.out.println("Constructor with param: " + param);
                     }
                 }
                \s""";

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("TestClass.java", javaCode);

        Set<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);

        assertEquals(2, methods.size());

        MethodMeta defaultConstructor = methods.stream()
                .filter(m -> m.signature().equals("()"))
                .findFirst()
                .orElse(null);
        assertNotNull(defaultConstructor);
        assertEquals("com.example.TestClass", defaultConstructor.classFqn());
        assertEquals("TestClass", defaultConstructor.methodName());

        MethodMeta paramConstructor = methods.stream()
                .filter(m -> m.signature().equals("(String)"))
                .findFirst()
                .orElse(null);
        assertNotNull(paramConstructor);
        assertEquals("com.example.TestClass", paramConstructor.classFqn());
        assertEquals("TestClass", paramConstructor.methodName());
    }

    public void testExtractNestedClassMethods() {
        String javaCode = """
                 package com.example;
                \s
                 public class OuterClass {
                     public void outerMethod() {
                         System.out.println("Outer");
                     }
                    \s
                     public static class InnerClass {
                         public void innerMethod() {
                             System.out.println("Inner");
                         }
                     }
                 }
                \s""";

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("OuterClass.java", javaCode);

        Set<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);

        assertEquals(2, methods.size());

        MethodMeta outerMethod = methods.stream()
                .filter(m -> m.methodName().equals("outerMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(outerMethod);
        assertEquals("com.example.OuterClass", outerMethod.classFqn());

        MethodMeta innerMethod = methods.stream()
                .filter(m -> m.methodName().equals("innerMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(innerMethod);
        assertEquals("com.example.OuterClass.InnerClass", innerMethod.classFqn());
    }

    public void testEmptyClass() {
        String javaCode = """
                package com.example;

                public class EmptyClass {
                }
                """;

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("EmptyClass.java", javaCode);

        Set<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);

        assertEquals(0, methods.size());
    }
}
