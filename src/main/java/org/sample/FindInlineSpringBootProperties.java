package org.sample;

import com.yourorg.table.SpringBootPropertyReport;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.internal.PropertyPlaceholderHelper;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class FindInlineSpringBootProperties extends Recipe {

  transient SpringBootPropertyReport report = new SpringBootPropertyReport(this);

  @Override
  public String getDisplayName() {
    return "Search for references to spring boot properties that are inlined in code";
  }

  @Override
  public String getDescription() {
    return "This recipe searches for references to spring boot properties that are inlined in code typically found in `@Value` and `@ConditionalOnProperty` annotations.";
  }

  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor() {
    return new JavaIsoVisitor<ExecutionContext>() {
      @Override
      public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext executionContext) {

        if (TypeUtils.isOfClassType(annotation.getType(), "org.springframework.beans.factory.annotation.Value")) {
          // Extract any properties from the @Value annotation and record them in the data table along with the source
          // file where the value annotation is found.
          Set<String> properties = extractPropertiesFromValueAnnotation(annotation);
          String sourcePath = getSourcePath();
          for (String property : properties) {
            report.insertRow(executionContext, new SpringBootPropertyReport.Row(property,sourcePath));
          }
        } else if (TypeUtils.isOfClassType(annotation.getType(), "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty")) {
          // Extract any properties from the @ConditionalOnProperty annotation and record them in the data table along with the source
          // file where the value annotation is found.
          Set<String> properties = extractPropertiesFromConditionalAnnotation(annotation);
          String sourcePath = getSourcePath();
          for (String property : properties) {
            report.insertRow(executionContext, new SpringBootPropertyReport.Row(property, sourcePath));
          }
        }
        return super.visitAnnotation(annotation, executionContext);
      }

      private String getSourcePath() {
        SourceFile sourceFile = getCursor().firstEnclosing(SourceFile.class);
        return sourceFile == null ? "" : sourceFile.getSourcePath().toString();
      }

    };
  }

  static PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}", ":");

  /**
   * Given an AST that represents the @Value annotation, extract the properties that are inlined in the expression.
   * This method leverages the {@link PropertyPlaceholderHelper} to parse and extract one or more properties from the
   * expression.
   *
   * @param annotation The AST that represents the @Value annotation
   * @return A set of properties that are inlined in the expression or an empty set if no properties are found
   */
  static Set<String> extractPropertiesFromValueAnnotation(J.Annotation annotation) {
    if (annotation.getArguments() != null && !annotation.getArguments().isEmpty()) {
      String expression = annotation.getArguments().get(0).toString();
      Set<String> properties = new HashSet<>();
      placeholderHelper.replacePlaceholders(expression, name -> {
        if (!name.contains("${")) {
          properties.add(name);
        }
        return null;
      });
      return properties;
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Given an AST that represents the @ConditionalOnProperty annotation, extract the properties that are inlined in the
   * expression. This method will handle both case where the property is inlined as a literal or when the annotation
   * has a `name` and `prefix` attribute. The name attribute is an array and can result in multiple properties.
   *
   * @param annotation The AST that represents the @ConditionalOnProperty annotation
   * @return A set of properties that are inlined in the expression or an empty set if no properties are found
   */
  private static Set<String> extractPropertiesFromConditionalAnnotation(J.Annotation annotation) {
    String prefix = "";
    Set<String> properties = Collections.emptySet();
    if (annotation.getArguments() != null) {
      for (Expression expression : annotation.getArguments()) {
        if (expression instanceof J.Literal) {
          properties = extractValuesFromExpression(expression);
        } else if (expression instanceof J.Assignment) {
          J.Assignment argument = (J.Assignment) expression;
          String name = argument.getVariable().toString();
          if ("prefix".equals(name)) {
            Set<String> values = extractValuesFromExpression(argument.getAssignment());
            prefix = values.isEmpty() ? "" : values.iterator().next();
          } else if ("name".equals(name)) {
            properties = extractValuesFromExpression(argument.getAssignment());
          }
        }
      }
    }
    if (!prefix.isEmpty()) {
      String finalPrefix = prefix;
      return properties.stream().map(p -> finalPrefix + "." + p).collect(Collectors.toSet());
    } else {
      return properties;
    }
  }

  private static Set<String> extractValuesFromExpression(Expression expression) {
    if (expression instanceof J.Literal) {
      String value = getStringFromLiteral((J.Literal) expression);
      if (value != null) {
        return Collections.singleton(value);
      }
    } else if (expression instanceof J.NewArray) {
      J.NewArray newArray = (J.NewArray) expression;
      if (newArray.getInitializer() != null) {
        return newArray.getInitializer().stream()
            .map(e -> {
              if (e instanceof J.Literal) {
                J.Literal literal = (J.Literal) e;
                if (literal.getValue() != null) {
                  return (String) literal.getValue();
                } else {
                  return "";
                }
              } else {
                return null;
              }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
      }
    }
    return Collections.emptySet();
  }

  @Nullable
  static String getStringFromLiteral(J.Literal literal) {
    return (String) literal.getValue();
  }
}
