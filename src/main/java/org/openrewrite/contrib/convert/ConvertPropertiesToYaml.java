package org.openrewrite.contrib.convert;


import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.contrib.PathUtils;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.YamlParser;
import org.openrewrite.yaml.tree.Yaml;

import java.nio.file.Path;
import java.util.*;


@Value
@EqualsAndHashCode(callSuper = false)
public class ConvertPropertiesToYaml extends ScanningRecipe<PropertiesAccumulator> {

  @Option(displayName = "File pattern",
      description = "A glob expression representing a file path (relative to the project root). Only files matching " +
          "the expression will be converted and blank/null matches all.",
      required = false,
      example = "**/resources/*.properties")
  String filePattern;

  @Option(displayName = "Sort property keys",
      description = "Sort the property keys in alphabetical order.",
      example = "com.google.guava",
      required = false)
  @Nullable
  Boolean sortKeys;

  @Option(displayName = "YAML suffix",
      description = "Which suffix to use for the converted YAML files. The default is 'yml'.",
      example = "yml",
      valid = {"yml", "yaml"},
      required = false)
  String yamlSuffix;

  public ConvertPropertiesToYaml(@Nullable String filePattern, @Nullable Boolean sortKeys, @Nullable String yamlSuffix) {
    //noinspection DataFlowIssue
    this.filePattern = PathUtils.normalize(filePattern == null ? "**" : filePattern);
    this.sortKeys = sortKeys;
    this.yamlSuffix = yamlSuffix == null ? "yml" : yamlSuffix;
  }

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
          if (PathUtils.matchesGlob(sourceFile.getSourcePath(), filePattern)) {
            // Only collect properties from files that match the file pattern
            acc.collectSourceFileProperties(sourceFile);
          }
        } else if (tree instanceof Yaml.Documents) {
          // Collect all yaml source files, as we might have a conflict if there is both a properties and yaml file
          // within the same directory.
          Yaml.Documents sourceFile = (Yaml.Documents) tree;
          acc.pathToSourceFile().put(sourceFile.getSourcePath(), sourceFile);
        }
        return super.visit(tree, executionContext);
      }
    };
  }

  @Override
  public Collection<? extends SourceFile> generate(PropertiesAccumulator acc, ExecutionContext ctx) {

    List<SourceFile> generated = new ArrayList<>();

    for (Map.Entry<Path, Map<String, Object>> entry : acc.pathToProperties().entrySet()) {
      Path targetPath = PathUtils.withExtension(entry.getKey(), yamlSuffix);

      if (acc.pathToSourceFile().containsKey(targetPath)) {
        // Don't overwrite existing yaml files
        continue;
      }

      YamlParser.builder().build().parse(YamlConverterUtils.toYaml(entry.getValue())).findFirst().ifPresent(
          yamlFile -> generated.add(yamlFile.withSourcePath(targetPath))
      );
    }

    return generated.isEmpty() ? Collections.emptySet() : generated;
  }

  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor(PropertiesAccumulator acc) {
    return new TreeVisitor<Tree, ExecutionContext>() {
      @Override
      public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext executionContext) {
        if (tree instanceof Properties.File) {
          Properties.File sourceFile = (Properties.File) tree;
          if (PathUtils.matchesGlob(sourceFile.getSourcePath(), filePattern)) {
            // Delete the properties file
            return null;
          }
        } else if (tree instanceof Yaml.Documents) {
          // TODO If the conversion of a properties file will result in a conflict with an existing yaml file, we should
          // merge the properties into the existing yaml file.
        }
        return super.visit(tree, executionContext);
      }
    };
  }
}
