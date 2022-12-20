package org.openrewrite.starter;

import org.junit.jupiter.api.Test;
import org.openrewrite.config.Environment;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class RemoveAnnotationsTest implements RewriteTest {

    @Test
    void removeAll() {
        rewriteRun(
          (spec) -> {
              Environment env = Environment.builder().scanRuntimeClasspath().build();
              spec
                .recipe(env.activateRecipes("example.RemoveDeprecatedAnnotations"))
                .parser(JavaParser.fromJavaVersion()
                  .logCompilationWarningsAndErrors(true)
                  );
          },
          java(
            """
                  import java.util.List;
                  
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      @Deprecated(since = "1.1.0")
                      public void methodB(List<String> values) {
                      }
                      @Deprecated(since = "1.2.0")
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
                      public void methodB(List<String> values) {
                      }
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """
            )
        );
    }

    @Test
    void remove11DeprecatedAnnotations() {
        rewriteRun(
          (spec) -> {
              Environment env = Environment.builder().scanRuntimeClasspath().build();
              spec
                .recipe(env.activateRecipes("example.RemoveDeprecated11Annotations"))
                .parser(JavaParser.fromJavaVersion()
                  .logCompilationWarningsAndErrors(true));
          },
          java(
            """
                  import java.util.List;
                  
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      @Deprecated(since = "1.1.0")
                      public void methodB(List<String> values) {
                      }
                      @Deprecated(since = "1.2.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """,
            """
                  import java.util.List;
            
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      public void methodB(List<String> values) {
                      }
                      @Deprecated(since = "1.2.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """
          )
        );
    }

    @Test
    void remove11DeprecatedAnnotationsOnly() {
        rewriteRun(
          (spec) -> {
              Environment env = Environment.builder().scanRuntimeClasspath().build();
              spec
                .recipe(env.activateRecipes("example.RemoveDeprecated11Annotations"))
                .parser(JavaParser.fromJavaVersion()
                  .logCompilationWarningsAndErrors(true));
          },
          java(
            """
                  import java.util.List;
                  
                  @Deprecated(since = "1.1.0")
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      @Deprecated(since = "1.1.0")
                      public void methodB(List<String> values) {
                      }
                      @Deprecated(since = "1.2.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """,
            """
                  import java.util.List;
            
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      public void methodB(List<String> values) {
                      }
                      @Deprecated(since = "1.2.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """
          )
        );
    }

    @Test
    void remove12DeprecatedAnnotationsFromMethodsOnly() {
        rewriteRun(
          (spec) -> spec
            .recipe(new RemoveAnnotationFromMethodsOnly(null, "@java.lang.Deprecated(since=\"1.2.0\")"))
            .parser(JavaParser.fromJavaVersion()
              .logCompilationWarningsAndErrors(true)),
          java(
            """
                  import java.util.List;

                  @Deprecated(since = "1.2.0")
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      @Deprecated(since = "1.2.0")
                      public void methodB(List<String> values) {
                      }
                      @Deprecated(since = "1.2.0")
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """,
            """
                  import java.util.List;
            
                  @Deprecated(since = "1.2.0")
                  class Test {
                      @Deprecated
                      public void methodA() {
                      }
                      public void methodB(List<String> values) {
                      }
                      public String methodC(List<String> values) {
                        return null;
                      }
                  }
              """
          )
        );
    }

}
