package org.openrewrite.contrib.convert;


import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.tree.Yaml;


@Value
@EqualsAndHashCode(callSuper = false)
public class ConvertPropertiesToYaml extends ScanningRecipe<PropertiesAccumulator> {

  @Option(displayName = "Sort property keys",
      description = "Sort the property keys in alphabetical order.",
      example = "com.google.guava",
      required = false)
  @Nullable
  Boolean sortKeys;

  @Override
  public String getDisplayName() {
      return "Convert properties to YAML";
  }

  @Override
  public String getDescription() {
      return "Converts application.properties to application.yml.";
  }

  @Override
  public PropertiesAccumulator getInitialValue(ExecutionContext ctx) {
    return new PropertiesAccumulator(sortKeys != null && sortKeys);
  }

  @Override
  public TreeVisitor<?, ExecutionContext> getScanner(PropertiesAccumulator acc) {
    return new TreeVisitor<Tree, ExecutionContext>() {
      @Override
      public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext executionContext) {
        if (tree instanceof Properties.File) {
          Properties.File sourceFile = (Properties.File) tree;
          acc.collectSourceFileProperties(sourceFile);
        } else if (tree instanceof Yaml.Documents) {
          Yaml.Documents sourceFile = (Yaml.Documents) tree;
          acc.collectSourceFileProperties(sourceFile);
        }
        return super.visit(tree, executionContext);
      }
    };
  }
}
