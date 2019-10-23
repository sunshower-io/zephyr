package io.sunshower.test.common;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.val;

public class Tests {

  public static File buildDirectory() {
    for (var file = current(); file != null; file = file.getParentFile()) {
      val check = new File(file, "build");
      if (check.exists() && check.isDirectory()) {
        return check.toPath().normalize().toFile();
      }
    }
    throw new NoSuchElementException(
        "No build directory found for : " + new File(".").getAbsolutePath());
  }

  public static File rootDirectory() {
    File result = null;
    for (var file = current(); file != null; file = file.getParentFile()) {
      val check = new File(file, "build.gradle");
      if (check.exists() && check.isFile()) {
        result = check.getParentFile();
      } else {
        break;
      }
    }
    if (result != null) {
      return result;
    }
    throw new NoSuchElementException("Failed to resolve root directory");
  }

  public static File projectOutput(String project, String type) {
    val projectBuild = project(project).toPath().resolve("build").toFile();
    if (!projectBuild.exists()) {
      throw new IllegalArgumentException(
          "Error:  Project "
              + project
              + " either didn't exist or hasn't been build (have you added it is a dependency?)");
    }
    if (!projectBuild.isDirectory()) {
      throw new IllegalArgumentException(
          "Error:  Project " + project + " isn't a directory--can't search");
    }
    val file = new File(projectBuild, "libs");

    val results = file.listFiles(t -> t.getPath().endsWith(type));
    if (results != null && results.length > 0) {
      return results[0];
    }
    throw new NoSuchElementException("Error--can't find project output");
  }

  public static File relativeToCurrentProjectBuild(String ext, String... segments) {
    return locate(currentRoot().toPath(), ext, segments);
  }

  public static File relativeToProjectBuild(String project, String ext, String... segments) {
    val p = project(project).toPath();
    return locate(p, ext, segments);
  }

  public static File currentRoot() {
    for (var file = current(); file != null; file = file.getParentFile()) {
      val b = new File(file, "build.gradle");
      if (b.exists()) {
        return file;
      }
    }
    throw new NoSuchElementException("No root dir?? Whack");
  }

  public static File createTemp() {
    return createTemp(UUID.randomUUID().toString());
  }

  public static File createTemp(String directory) {
    val tmp = new File(buildDirectory(), "temp");
    val result = new File(tmp, directory);
    if (!result.mkdirs()) {
      if (!result.exists()) {
        throw new IllegalStateException(
            "Failed to create temp directory " + result.getAbsolutePath());
      }
    }
    result.deleteOnExit();
    return result.getAbsoluteFile();
  }

  public static File project(String project) {
    val root = rootDirectory();
    val segments = project.split(":");
    File result = root;
    for (val segment : segments) {
      result = new File(result, segment);
      if (!(result.exists() && result.isDirectory())) {
        throw new NoSuchElementException(
            "Can't find project with path: "
                + project
                + " in "
                + root.getAbsolutePath()
                + " checked ("
                + result.getAbsolutePath()
                + ")");
      }
    }
    return result;
  }

  private static File locate(Path u, String ext, String... segments) {
    val p = u.resolve("build").resolve(String.join(File.separator, segments)).normalize().toFile();
    if (!p.exists()) {
      throw new NoSuchElementException(format("File with path %s does not exist", p));
    }

    val f = p.listFiles(t -> t.getName().endsWith(ext));
    if (f == null || f.length == 0) {
      throw new NoSuchElementException(format("File with path %s/*.%s does not exist", p, ext));
    }
    return f[0];
  }

  private static File current() {
    return new File(".").getAbsoluteFile();
  }
}
