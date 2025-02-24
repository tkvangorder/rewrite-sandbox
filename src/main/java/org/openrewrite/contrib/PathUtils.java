package org.openrewrite.contrib;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {

  @Nullable
  public static String normalize(@Nullable String filePattern) {
    if (filePattern == null) {
      return null;
    } else if (filePattern.startsWith("./") || filePattern.startsWith(".\\")) {
      return filePattern.substring(2);
    } else if (filePattern.startsWith("/") || filePattern.startsWith("\\")) {
      return filePattern.substring(1);
    }
    return filePattern;
  }

  public static boolean matchesGlob(@Nullable Path path, @Nullable String globPattern) {
    return org.openrewrite.PathUtils.matchesGlob(path, globPattern);
  }

  public static Path withExtension(Path path, String extension) {
    String filename = path.getFileName().toString();
    int dotIndex = filename.lastIndexOf('.');

    // Remove existing extension if present
    String nameWithoutExtension = (dotIndex == -1) ? filename : filename.substring(0, dotIndex);

    // Add new extension (ensuring it starts with a dot)
    String newExtension = extension.startsWith(".") ? extension : "." + extension;
    String newFilename = nameWithoutExtension + newExtension;

    return path.getParent() == null
        ? Paths.get(newFilename)
        : path.getParent().resolve(newFilename);
  }
}
