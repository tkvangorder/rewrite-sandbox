package org.sample;

import org.sample.table.SpringBootPropertyReport;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.sample.table.SpringBootPropertyReport.Type;
import static org.sample.table.SpringBootPropertyReport.Row;

class FindInlineSpringBootPropertiesTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .recipe(new FindInlineSpringBootProperties(false))
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
            // The second way to add types is add a .jar file from the classpath, this is finding the jar file by
            // parsing the classpath and finding the .jar file location.
            .classpath("lombok-1.18.36")
          );
    }

    // NOTE, this test also includes Lombok's Value annotation to demonstrate the importance of type information.

    @Test
    void findInlineValueAnnotation() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new Row("my.property", Type.VALUE, "org/cool/MyClass.java")
          )),
          //language=java
          java(
            """
              package org.cool;
              
              import org.springframework.beans.factory.annotation.Value;

              @lombok.Value
              public class MyClass {
                  @Value("${my.property}")
                  String myProperty;
              }
              """
          )
        );
    }

    @Test
    void findNestedInlineValueAnnotation() {
        rewriteRun(
          spec -> spec.dataTable(SpringBootPropertyReport.Row.class, rows -> assertThat(rows).containsExactly(
            new Row("my.property", Type.VALUE, "org/cool/MyClass.java"),
            new Row("my.other.property", Type.VALUE, "org/cool/MyClass.java")
          )),
          //language=java
          java(
            """
              package org.cool;
              import org.springframework.beans.factory.annotation.Value;

              public class MyClass {
                  @Value("${my.property}-${my.other.property}:default}")
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
            new Row("my.property", Type.CONDITIONAL, "MyClass.java")
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
            new Row("my.property", Type.CONDITIONAL, "MyClass.java")
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
            new Row("my.property", Type.CONDITIONAL, "MyClass.java")
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
            new Row("my.property", Type.CONDITIONAL, "MyClass.java"),
        new Row("my.other-property", Type.CONDITIONAL, "MyClass.java")
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

    @Test
    void indirectPropertyName() {
        rewriteRun(
          //language=java
          java(
            """
              package org.cool;
              
              import org.springframework.beans.factory.annotation.Value;

              public class MyConfiguration {
    
                public static final String MY_PROPERTY = "my.property";
        
                public class MyClass {
                  @Value(MY_PROPERTY)
                  private String myProperty;
                }
              }
              """,
            """
              package org.cool;
              
              import org.springframework.beans.factory.annotation.Value;

              public class MyConfiguration {
    
                public static final String MY_PROPERTY = "my.property";
        
                public class MyClass {
                  /*~~(Annotation found but could not extract property.)~~>*/@Value(MY_PROPERTY)
                  private String myProperty;
                }
              }
              """
          )
        );
    }

}