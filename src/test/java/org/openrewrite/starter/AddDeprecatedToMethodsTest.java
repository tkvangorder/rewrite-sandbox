package org.openrewrite.starter;


import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;

class AddDeprecatedToMethodsTest implements RewriteTest {

    //Note, you can define defaults for the RecipeSpec and these defaults will be used for all tests.
    //In this case, the recipe and the parser are common.
    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .recipe(new AddDeprecatedToMethods(null))
          .parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true));
    }

    @Test
    void parseSomeJava() {
        JavaParser parser = JavaParser.fromJavaVersion()
          .logCompilationWarningsAndErrors(true).build();

        List<J.CompilationUnit> compilationUnits =parser.parse(
          """
                  package com.example;
                  import java.util.Lis t;
                  
                  public class Example {
                      @Deprecated(since = "1.1.0")
                      public void methodA() {
                      }
                      @Deprecated(since = "1.1.0")
                      public void methodB(List<String> values) {
                        for (String value : values) {
                            System.out.println(value + " was here.");
                        }
                      }
                      @Deprecated(since = "1.1.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                      public static class Inner {
                        public String methodD(String value) {
                            return value;
                        }
                      }
                  }
                """,
                """
                    package com.example2;
                    import com.example.Example;
                    class AnotherExample {
                        private Example aReference = new Example();
                        
                        private Example.Inner innerReference = new Example.Inner();
                    }
                """);

        assertThat(compilationUnits).hasSize(2);
    }

    @Test
    void addToAllMethods() {
        rewriteRun(
          java(
            """
                  import java.util.List;

                  class Test {
                      public void methodA() {
                      }
                      public void methodB(List<String> values) {
                      }
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """,
            """
                  import java.util.List;
                  
                  class Test {
                      @Deprecated(since = "1.1.0")
                      public void methodA() {
                      }
                  \n
                      @Deprecated(since = "1.1.0")
                      public void methodB(List<String> values) {
                      }
                  \n
                      @Deprecated(since = "1.1.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """
          )
        );
    }

    @Test
    void addToSpecificMethods() {
        rewriteRun(
          (spec) -> spec
            .recipe(new AddDeprecatedToMethods("* *(java.util.List)"))
            .parser(JavaParser.fromJavaVersion()
              .logCompilationWarningsAndErrors(true)),
          java(
            """
            import java.util.List;
            
            class Test {
                public void methodA() {
                }
                public void methodB(List<String> values) {
                }
                public String methodC(List<String> values) {
                  return null;
                }
            }
        """,
            """
                  import java.util.List;
                  
                  class Test {
                      public void methodA() {
                      }
                  \n
                      @Deprecated(since = "1.1.0")
                      public void methodB(List<String> values) {
                      }
                  \n
                      @Deprecated(since = "1.1.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """
          )
        );
    }

}
