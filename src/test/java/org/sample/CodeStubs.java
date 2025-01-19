package org.sample;

import org.intellij.lang.annotations.Language;

public class CodeStubs {

    @Language("java")
    public final static String SPRING_CONDITIONAL_ON_PROPERTY = """
        package org.springframework.boot.autoconfigure.condition;
      
        import java.lang.annotation.Documented;
        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;
      
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.TYPE, ElementType.METHOD})
        @Documented
        public @interface ConditionalOnProperty {
          String[] value() default {};
          String prefix() default "";
          String[] name() default {};
          String havingValue() default "";
          boolean matchIfMissing() default false;
        }
        """;

    @Language("java")
    public final static String SPRING_VALUE = """
    package org.springframework.beans.factory.annotation;
  
    import java.lang.annotation.Documented;
    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;
  
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Value {
      String value();
    }
    """;
}
