package com.github.mindpazi.contextinaitool.psi;

import com.github.mindpazi.contextinaitool.model.MethodMeta;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

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
        
        List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
        
        assertEquals(1, methods.size());
        
        MethodMeta method = methods.getFirst();
        assertEquals("com.example.TestClass", method.classFqn());
        assertEquals("simpleMethod", method.methodName());
        assertEquals(4, method.startLine());
        assertEquals(6, method.endLine());
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
        
        List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
        
        assertEquals(3, methods.size());
        
        MethodMeta first = methods.getFirst();
        assertEquals("com.example.TestClass", first.classFqn());
        assertEquals("firstMethod", first.methodName());
        assertEquals(4, first.startLine());
        assertEquals(6, first.endLine());
        
        MethodMeta second = methods.get(1);
        assertEquals("com.example.TestClass", second.classFqn());
        assertEquals("secondMethod", second.methodName());
        assertEquals(8, second.startLine());
        assertEquals(10, second.endLine());
        
        MethodMeta third = methods.get(2);
        assertEquals("com.example.TestClass", third.classFqn());
        assertEquals("thirdMethod", third.methodName());
        assertEquals(12, third.startLine());
        assertEquals(14, third.endLine());
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
        
        List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
        
        assertEquals(2, methods.size());
        
        MethodMeta defaultConstructor = methods.getFirst();
        assertEquals("com.example.TestClass", defaultConstructor.classFqn());
        assertEquals("TestClass", defaultConstructor.methodName());
        assertEquals(4, defaultConstructor.startLine());
        assertEquals(6, defaultConstructor.endLine());
        
        MethodMeta paramConstructor = methods.get(1);
        assertEquals("com.example.TestClass", paramConstructor.classFqn());
        assertEquals("TestClass", paramConstructor.methodName());
        assertEquals(8, paramConstructor.startLine());
        assertEquals(10, paramConstructor.endLine());
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
        
        List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
        
        assertEquals(2, methods.size());
        
        MethodMeta outerMethod = methods.stream()
                .filter(m -> m.methodName().equals("outerMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(outerMethod);
        assertEquals("com.example.OuterClass", outerMethod.classFqn());
        assertEquals(4, outerMethod.startLine());
        assertEquals(6, outerMethod.endLine());
        
        MethodMeta innerMethod = methods.stream()
                .filter(m -> m.methodName().equals("innerMethod"))
                .findFirst()
                .orElse(null);
        assertNotNull(innerMethod);
        assertEquals("com.example.OuterClass.InnerClass", innerMethod.classFqn());
        assertEquals(9, innerMethod.startLine());
        assertEquals(11, innerMethod.endLine());
    }

    public void testEmptyClass() {
        String javaCode = """
                package com.example;
                
                public class EmptyClass {
                }
                """;

        PsiJavaFile javaFile = (PsiJavaFile) myFixture.configureByText("EmptyClass.java", javaCode);
        
        List<MethodMeta> methods = JavaMethodExtractor.extract(javaFile);
        
        assertEquals(0, methods.size());
    }
}
