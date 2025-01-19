package org.sample;

import com.yourorg.table.SpringBootPropertyReport;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;

class FindInlineSpringBootPropertiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .recipe(new FindInlineSpringBootProperties())
          // The parser here is used to parse the "before" test case and must be aware of all types used in the test case
          // to get proper type attribution. Remember type attribution is OpenRewrite's superpower.
          .parser(JavaParser
            .fromJavaVersion()
            // There are three ways to add types that are needed by the  parser. The first is to use "dependsOn" and stub
            // out any types that are needed. This is obviously more space efficient, but you are duplicating the source
            // that may belong to a library.
            .dependsOn(
              CodeStubs.SPRING_VALUE,
              CodeStubs.SPRING_CONDITIONAL_ON_PROPERTY
            )
          );
    }

    @Test
    void findInlineValueAnnotation() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new SpringBootPropertyReport.Row("my.property", "org/cool/MyClass.java")
          )),
          //language=java
          java(
            """
              package org.cool;
              import org.springframework.beans.factory.annotation.Value;

              public class MyClass {
                  @Value("${my.property}")
                  private String myProperty;
              }
              """
          )
        );
    }

    @Test
    void findNestedInlineValueAnnotation() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new SpringBootPropertyReport.Row("my.property", "org/cool/MyClass.java"),
            new SpringBootPropertyReport.Row("my.other.property", "org/cool/MyClass.java")
          )),
          //language=java
          java(
            """
              package org.cool;
              import org.springframework.beans.factory.annotation.Value;

              public class MyClass {
                  @Value("${my.property:${my.other.property}:default}")
                  private String myProperty;
              }
              """
          )
        );
    }

    @Test
    void conditionalOnPropertyValue() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new SpringBootPropertyReport.Row("my.property", "MyClass.java")
          )),
          //language=java
          java(
            """
              import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

              @ConditionalOnProperty("my.property")
              public class MyClass {
              }
              """
          )
        );
    }

    @Test
    void conditionalOnPropertyNameAndPrefix() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new SpringBootPropertyReport.Row("my.property", "MyClass.java")
          )),
          //language=java
          java(
            """
              import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

              @ConditionalOnProperty(name="property", prefix="my")
              public class MyClass {
              }
              """
          )
        );
    }

    @Test
    void conditionalOnPropertyNameArrayAndPrefix() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new SpringBootPropertyReport.Row("my.property", "MyClass.java")
          )),
          //language=java
          java(
            """
              import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

              @ConditionalOnProperty(name={"property"}, prefix="my")
              public class MyClass {
              }
              """
          )
        );
    }

    @Test
    void conditionalOnPropertyTwoProperties() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new SpringBootPropertyReport.Row("my.property", "MyClass.java"),
            new SpringBootPropertyReport.Row("my.other-property", "MyClass.java")
          )),
          //language=java
          java(
            """
              import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
             
              @ConditionalOnProperty(name={"property", "other-property"}, prefix="my")
              public class MyClass {
              }
              """
          )
        );
    }
}