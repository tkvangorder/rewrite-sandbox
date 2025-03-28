package org.openrewrite.contrib.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.openrewrite.SourceFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LstSerializer {

  private final ObjectMapper objectMapper;

  public LstSerializer() {

    objectMapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()
        .registerModule(new ParameterNamesModule())
        .registerModule(new JavaTimeModule());

    objectMapper.coercionConfigFor(LogicalType.Collection)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull)
        .setAcceptBlankAsEmpty(true);
    objectMapper.setVisibility(
        objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withCreatorVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
    );
  }

  public void serialize(List<SourceFile> sourceFiles, OutputStream outputStream) {
    try {
      objectMapper.writeValue(outputStream, sourceFiles);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<SourceFile> deserialize(InputStream inputStream) {
    try {
      return objectMapper.readValue(inputStream, new TypeReference<List<SourceFile>>() {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<SourceFile> deserialize(byte[] bytes) {
    try {
      return objectMapper.readValue(bytes, new TypeReference<List<SourceFile>>() {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] serialize(List<SourceFile> sourceFiles) {
    try {
      SourceFileList typeAwareList = new SourceFileList(sourceFiles);
      return objectMapper.writeValueAsBytes(typeAwareList);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This is to get around type erasure issues when serializing a list of SourceFile. If you attempt to directly
   * serialize the List<SourceFile>, Jackson will not be able to determine the type information for the elements in the list.
   * This is only a problem when passing a List as the root into the ObjectMapper.
   */
  private static class SourceFileList extends ArrayList<SourceFile> {
      // This is a simple wrapper to allow for custom serialization if needed
      // and to ensure that the list can be serialized properly by Jackson
      public SourceFileList(List<SourceFile> sourceFiles) {
          super(sourceFiles);
      }

      public SourceFileList() {
          super();
      }
  }
}
