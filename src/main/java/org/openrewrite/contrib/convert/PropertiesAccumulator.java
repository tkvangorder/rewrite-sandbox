package org.openrewrite.contrib.convert;

import lombok.Value;
import lombok.experimental.Accessors;
import org.openrewrite.SourceFile;
import org.openrewrite.properties.PropertiesVisitor;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.nio.file.Path;
import java.util.*;

@Value
@Accessors(fluent = true)
public class PropertiesAccumulator {

  Map<Path, Map<String, Object>> pathToProperties = new HashMap<>();
  Map<Path, SourceFile>  pathToSourceFile = new HashMap<>();

  boolean sorted;

  /**
   * @param sorted If true, the properties will be sorted by key, otherwise the properties will be maintained in the
   *               order they were found.
   */
  public PropertiesAccumulator(boolean sorted) {
    this.sorted = sorted;
  }

  public void collectSourceFileProperties(Properties.File sourceFile) {
    Map<String, Object> propertyMap = sorted ? new TreeMap<>() : new LinkedHashMap<>();
    List<String> accumulatedComments = new ArrayList<>();
    new PropertiesVisitor<Map<String, Object>>() {
      @Override
      public org.openrewrite.properties.tree.Properties visitEntry(Properties.Entry entry, Map<String, Object> stringObjectMap) {
        addProperty(stringObjectMap, Arrays.asList(entry.getKey().split("\\.")), entry.getValue().getText(), new ArrayList<>(accumulatedComments));
        accumulatedComments.clear();
        return super.visitEntry(entry, stringObjectMap);
      }

      @Override
      public Properties visitComment(Properties.Comment comment, Map<String, Object> stringObjectMap) {
        accumulatedComments.add(comment.getMessage());
        return super.visitComment(comment, stringObjectMap);
      }
    }.visit(sourceFile, propertyMap);
    pathToProperties.put(sourceFile.getSourcePath(), propertyMap);
  }

  public void collectSourceFileProperties(Yaml.Documents sourceFile) {
    Map<String, Object> propertyMap = sorted ? new TreeMap<>() : new LinkedHashMap<>();
    List<String> accumulatedComments = new ArrayList<>();
    new YamlVisitor<Map<String, Object>>() {
      private final Deque<String> path = new ArrayDeque<>();

      @Override
      public Yaml visitMapping(Yaml.Mapping mapping, Map<String, Object> stringObjectMap) {
        accumulatedComments.addAll(YamlConverterUtils.extractComments(mapping.getPrefix()));
        return super.visitMapping(mapping, stringObjectMap);
      }

      @Override
      public Yaml visitMappingEntry(Yaml.Mapping.Entry entry, Map<String, Object> stringObjectMap) {
        path.addLast(entry.getKey().getValue());
        Yaml y = super.visitMappingEntry(entry, stringObjectMap);
        path.removeLast();
        return y;
      }

      @Override
      public Yaml visitScalar(Yaml.Scalar scalar, Map<String, Object> stringObjectMap) {
        addProperty(propertyMap, new ArrayList<>(path), scalar.getValue(), Collections.unmodifiableList(accumulatedComments));
        accumulatedComments.clear();
        return super.visitScalar(scalar, stringObjectMap);
      }
    }.visit(sourceFile, propertyMap);
    pathToProperties.put(sourceFile.getSourcePath(), propertyMap);
  }

  private void addProperty(Map<String, Object> propertyMap, List<String> propertyParts, Object value, List<String> comments) {
    Map<String, Object> currentMap = propertyMap;

    Deque<String> path = new ArrayDeque<>();
    for (Iterator<String> index = propertyParts.iterator(); index.hasNext(); ) {
      String part = index.next();
      if (index.hasNext()) {
        path.addLast(part);
        Object existing = currentMap.computeIfAbsent(part, k -> sorted ? new TreeMap<String,Object>() : new LinkedHashMap<String,Object>());

        if (existing instanceof Map) {
          //noinspection unchecked
          currentMap = (Map<String, Object>) existing;
        } else {
          // Cannot create a path through a non-map value
          throw new IllegalArgumentException(
              "Cannot create property '" + String.join(".", propertyParts) + "' because '" +
                  String.join(".", path)+ "' is not a map"
          );
        }
      }
    }
    // Set the value at the leaf node
    currentMap.put(propertyParts.get(propertyParts.size() - 1), new PropertyValue(comments, value));
  }

}
