package org.openrewrite.contrib.convert;

import lombok.Value;

import java.util.List;

/**
 * A property value with comments agnostic of the file type.
 */
@Value
public class PropertyValue {
  List<String> comments;
  Object value;
}
