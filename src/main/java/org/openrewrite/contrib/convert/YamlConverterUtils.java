package org.openrewrite.contrib.convert;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlConverterUtils {
  private static final String INDENT = "  ";

  private static final Pattern COMMENT_PATTERN = Pattern.compile("#(.*)$", Pattern.MULTILINE);

  /**
   * Extracts comments from a YAML prefix string
   * @param prefix The Yaml prefix string
   * @return A lit of comments extracted from the prefix
   */
  public static List<String> extractComments(@Nullable String prefix) {
    if (prefix == null || prefix.trim().isEmpty()) {
      return Collections.emptyList();
    }

    List<String> comments = new ArrayList<>();
    Matcher matcher = COMMENT_PATTERN.matcher(prefix);

    while (matcher.find()) {
      String comment = matcher.group(1).trim();
      comments.add(comment);
    }

    return comments;
  }


  public static String toYaml(Map<String, Object> map) {
    //noinspection ConstantValue
    if (map == null) {
      throw new IllegalArgumentException("Map cannot be null");
    }
    StringBuilder yaml = new StringBuilder();
    convertMapToYaml(map, 0, yaml);
    return yaml.toString();
  }

  private static void convertMapToYaml(Map<String, Object> map, int depth, StringBuilder yaml) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof PropertyValue) {
        for (String comment : ((PropertyValue) value).getComments()) {
          appendIndentation(depth, yaml);
          yaml.append("#").append(comment).append("\n");
        }
      }
      appendIndentation(depth, yaml);
      yaml.append(entry.getKey()).append(":");

      if (value instanceof PropertyValue) {
        Object propertyValue = ((PropertyValue) value).getValue();
        if (propertyValue instanceof String) {
          formatString((String) propertyValue, yaml);
        } else {
          formatList((List<?>) propertyValue, depth + 1, yaml);
        }
      } else if (value instanceof Map) {
        yaml.append("\n");
        @SuppressWarnings("unchecked")
        Map<String, Object> nestedMap = (Map<String, Object>) value;
        convertMapToYaml(nestedMap, depth + 1, yaml);
      } else {
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
      }
    }
  }

  private static void formatList(List<?> list, int depth, StringBuilder yaml) {
    for (Object item : list) {
      appendIndentation(depth, yaml);
      yaml.append("- ");
      if (item instanceof Map) {
        yaml.append("\n");
        @SuppressWarnings("unchecked")
        Map<String, Object> mapItem = (Map<String, Object>) item;
        convertMapToYaml(mapItem, depth + 1, yaml);
      } else if (item instanceof List) {
        yaml.append("\n");
        formatList((List<?>) item, depth + 1, yaml);
      } else if (item instanceof String) {
        formatString((String) item, yaml);
      } else {
        yaml.append(item).append("\n");
      }
    }
  }

  private static void formatString(String value, StringBuilder yaml) {
    boolean needsQuotes = value.isEmpty() ||
        value.contains("\"") ||
        value.contains("'") ||
        value.contains(":") ||
        value.contains("\n") ||
        value.contains("#") ||
        value.startsWith(" ") ||
        value.endsWith(" ");

    if (needsQuotes) {
      if (value.contains("\"")) {
        yaml.append(" '").append(value).append("'\n");
      } else {
        yaml.append(" \"").append(value).append("\"\n");
      }
    } else {
      yaml.append(" ").append(value).append("\n");
    }
  }

  private static void appendIndentation(int depth, StringBuilder yaml) {
    for (int i = 0; i < depth; i++) {
      yaml.append(INDENT);
    }
  }
}
