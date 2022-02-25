package io.sunshower.test.common;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import lombok.val;

public class Tests {

  static final Set<String> classifiers = Set.of("sources", "javadoc", "source", "tests");

  static final Predicate<File> defaultPredicate =
      file -> classifiers.stream().noneMatch(classifier -> file.getName().contains(classifier));

  public static File buildDirectory() {
    for (var file = current(); file != null; file = file.getParentFile()) {
      val check = checkDirs(file, "build", "target", "out");

      if (check != null) {
        return check.toPath().normalize().toFile();
      }
    }
    throw new NoSuchElementException(
        "No build directory found for : " + new File(".").getAbsolutePath());
  }

  static boolean checkDir(File parent, String fileName) {
    val file = new File(parent, fileName);
    return file.exists() && file.isDirectory();
  }

  static File checkDirs(File parent, String... args) {
    for (val arg : args) {
      if (checkDir(parent, arg)) {
        return new File(parent, arg);
      }
    }
    return null;
  }

  public static File rootDirectory() {
    File result = null;
    for (var file = current(); file != null; file = file.getParentFile()) {

      var check = new File(file, "build.gradle");
      if (check.exists() && check.isFile()) {
        result = check.getParentFile();
      } else {
        check = new File(file, "pom.xml");
        if (check.exists() && check.isFile()) {
          result = check.getParentFile();
        }
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

  @SneakyThrows
  public static File relativeToCurrentProjectBuild(
      String ext, Predicate<File> filter, String... segments) {
    return locate(Paths.get(currentRoot().getCanonicalPath()), ext, filter, segments);
  }

  @SneakyThrows
  public static File relativeToCurrentProjectBuild(String ext, String... segments) {
    return relativeToCurrentProjectBuild(ext, defaultPredicate, segments);
    //    return locate(Paths.get(currentRoot().getCanonicalPath()), ext, segments);
  }

  public static File relativeToProjectBuild(String project, String ext, String... segments) {
    val p = project(project).toPath();
    return locate(p, ext, defaultPredicate, segments);
  }

  @SneakyThrows
  public static File relativeToProjectBuildWithCopy(
      File tempDir, String project, String ext, String... segments) {
    val existing = relativeToProjectBuild(project, ext, segments);
    val newDir = new File(tempDir, UUID.randomUUID().toString());
    newDir.mkdirs();
    val newFile = new File(newDir, existing.getName());
    Files.copy(existing.toPath(), newFile.toPath());
    return newFile;
  }

  public static File relativeToProjectBuild(
      String project, String ext, Predicate<File> predicate, String... segments) {
    val p = project(project).toPath();
    return locate(p, ext, predicate, segments);
  }

  public static File currentRoot() {
    for (var file = current(); file != null; file = file.getParentFile()) {
      var b = new File(file, "build.gradle");
      if (b.exists()) {
        return file;
      }

      b = new File(file, "pom.xml");
      if (b.exists()) {
        return file;
      }
    }
    throw new NoSuchElementException("No root dir?? Whack");
  }

  public static File createTemp() {
    return createTemp(UUID.randomUUID().toString());
  }

  public enum OS {
    WINDOWS,
    LINUX,
    MAC,
    SOLARIS
  }

  private static OS os = null;

  public static OS getOS() {
    if (os == null) {
      String operSys = System.getProperty("os.name").toLowerCase();
      if (operSys.contains("win")) {
        os = OS.WINDOWS;
      } else if (operSys.contains("nix") || operSys.contains("nux") || operSys.contains("aix")) {
        os = OS.LINUX;
      } else if (operSys.contains("mac")) {
        os = OS.MAC;
      } else if (operSys.contains("sunos")) {
        os = OS.SOLARIS;
      }
    }
    return os;
  }

  @SneakyThrows
  public static File createTemp(String directory) {
    val tmp = new File(buildDirectory(), "temp");
    val result = new File(tmp, directory);
    if (!result.mkdirs()) {
      if (!result.exists()) {
        throw new IllegalStateException(
            "Failed to create temp directory " + result.getAbsolutePath());
      }
    }

    if (getOS() == OS.WINDOWS) {
      AclFileAttributeView view =
          Files.getFileAttributeView(result.toPath(), AclFileAttributeView.class);
      final String AUTHENTICATED_USERS = "NT AUTHORITY\\Authenticated Users";

      val acls = view.getAcl();
      for (int i = 0; i < acls.size(); i++) {
        UserPrincipal principal = acls.get(i).principal();
        String principalName = principal.getName();
        if (principalName.equals(AUTHENTICATED_USERS)) {
          val permissions = acls.get(i).permissions();
          val p = EnumSet.allOf(AclEntryPermission.class);
          permissions.addAll(p);

          AclEntry entry =
              AclEntry.newBuilder()
                  .setType(AclEntryType.ALLOW)
                  .setPrincipal(principal)
                  .setPermissions(permissions)
                  .build();

          acls.set(i, entry);
        }
      }
      view.setAcl(acls);
    } else {

      if (!result.setExecutable(true)) {
        throw new IllegalStateException(
            String.format("Failed to set permission executable on '%s'", result));
      }

      if (!result.setReadable(true)) {
        throw new IllegalStateException(
            String.format("Failed to set permission read on '%s'", result));
      }

      if (!result.setWritable(true)) {
        throw new IllegalStateException(
            String.format("Failed to set permission write on '%s'", result));
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

  private static File locate(Path u, String ext, Predicate<File> filter, String... segments) {
    val p = u.resolve("build").resolve(String.join(File.separator, segments)).normalize().toFile();
    if (!p.exists()) {
      throw new NoSuchElementException(format("File with path %s does not exist", p));
    }

    val f = p.listFiles(t -> t.getName().endsWith(ext));
    if (f == null || f.length == 0) {
      throw new NoSuchElementException(format("File with path %s/*.%s does not exist", p, ext));
    }
    return Arrays.stream(f).filter(filter).findAny().orElseThrow();
  }

  private static File current() {
    return new File(".").getAbsoluteFile();
  }
}
