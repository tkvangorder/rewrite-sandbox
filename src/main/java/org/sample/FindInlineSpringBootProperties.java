package org.sample;

import com.yourorg.table.SpringBootPropertyReport;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.PropertyPlaceholderHelper;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
          if (annotation.getArguments() != null && !annotation.getArguments().isEmpty()) {
            Set<String> properties = extractPropertiesFromExpression(annotation.getArguments().get(0).toString());
            String sourcePath = getSourcePath();
            for (String property : properties) {
              report.insertRow(executionContext, new SpringBootPropertyReport.Row(property,sourcePath));
            }
          }
        } else if (TypeUtils.isOfClassType(annotation.getType(), "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty")) {
          if (annotation.getArguments() != null) {
            String prefix = "";
            Set<String> properties;
            for (Expression expression : annotation.getArguments()) {
              J.Assignment argument = (J.Assignment) expression;
              if (argument.) {
                continue;
            }
              if (argument.getName().equals("name")) {
                properties = extractPropertiesFromExpression(argument.getValue().printTrimmed());
              } else if (argument.getName().equals("prefix")) {
                prefix = argument.getValue().printTrimmed();
              }

          }
          String property = extractPropertyFromConditionalOnProperty(annotation);
          if (property != null) {
            report.insertRow(executionContext, new SpringBootPropertyReport.Row(
                property,
                getSourcePath()
            ));
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

  static Set<String> extractPropertiesFromExpression(String expression) {

    Set<String> properties = new HashSet<>();
    placeholderHelper.replacePlaceholders(expression, name -> {
      if (!name.contains("${")) {
        properties.add(name);
      }
      return null;
    });
    return properties;
  }

  String extractPropertyFromConditionalOnProperty(J.Annotation annotation) {
    return annotation.getArguments().stream()
        .map(yo -> yo.toString())
        .findAny().get();
  }
}
