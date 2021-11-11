package io.sunshower.nativelibs;

import static java.lang.String.format;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.val;

public class DLLEnumerator {

  static final Set<String> whitelist = new LinkedHashSet<>();

  static {
    Set<String> dlls = new LinkedHashSet<>();
    dlls.add("setupapi.dll");
    dlls.add("USER32.dll");
    dlls.add("sechost.dll");
    dlls.add("rpcrt4.dll");

    for (val dll : dlls) {
      whitelist.add(dll);
      whitelist.add(dll + ".so");
    }
  }

  enum PathType {
    File,
    Directory
  }

  static final String iconName = "installer256px.ico";
  static final String targetBinary = "rcedit-x86.exe";
  /**
   * @param args args[0]: The name of the binary to test args[1]: the directory tree of DLLs
   *     algorithm: for each file f in tree run T on I: expected to work copy binary to T run T
   *     without I: if fails: add I to required list if succeeded: add I to optional list
   */
  public static void main(String[] args) {

    if (args.length != 2) {
      System.out.println(
          "Usage:  java -jar DLLEnumerator <executable to test> <directory to scan>");
      return;
    }

    val binaryPath = getPath(args[0], PathType.File);
    val directoryPath = getPath(args[1], PathType.Directory);

    if (binaryPath == null) {
      System.out.format(
          "Error:  Path %s to binary must exist and be an executable file \n", args[0]);
      return;
    }

    if (directoryPath == null) {
      System.out.format("Error: Path %s to DLL directory must exist and be a directory\n", args[1]);
    }

    try {
      enumerate(binaryPath, directoryPath);
    } catch (Exception ex) {
      System.out.format("Encountered error: %s \n", ex.getMessage());
      ex.printStackTrace();
    }
  }

  private static void enumerate(Path binaryPath, Path directoryPath) throws IOException {
    val visitor = new DLLTestingVisitor(binaryPath);
    Files.walkFileTree(directoryPath, visitor);
    handleResults(visitor);
  }

  private static void handleResults(DLLTestingVisitor visitor) throws IOException {
    val optional = visitor.optionalLibs;
    val required = visitor.requiredLibs;

    System.out.println("Required Libs:");

    try (val opt = Files.newBufferedWriter(Paths.get("required.txt"))) {
      for (val optionalLib : required) {
        opt.write(optionalLib.toString());
        opt.write("\n");
      }
    }

    System.out.println("Optional Libs:");

    try (val opt = Files.newBufferedWriter(Paths.get("optional.txt"))) {
      for (val optionalLib : optional) {
        opt.write(optionalLib.toString());
        opt.write("\n");
      }
    }

    for (val optionalLib : optional) {
      System.out.println("Removing file: " + optionalLib);
      Files.deleteIfExists(optionalLib);
    }
  }

  static Path getPath(String dir, PathType directory) {
    val path = Paths.get(dir);
    if (Files.exists(path)) {
      switch (directory) {
        case File:
          {
            if (Files.isRegularFile(path)) {
              System.out.format("Directory '%s' exists\n", path);
              return path;
            }
          }
        case Directory:
          if (Files.isDirectory(path)) {
            System.out.format("Directory '%s' exists\n", path);
            return path;
          }
      }
    }
    return null;
  }

  static class TestResult {
    final int value;

    public TestResult(int result) {
      this.value = result;
    }
  }

  static class DLLTestingVisitor implements FileVisitor<Path> {

    final Path binaryPath;
    final Set<Path> requiredLibs;
    final Set<Path> optionalLibs;

    DLLTestingVisitor(Path binaryPath) {
      this.binaryPath = binaryPath;
      this.requiredLibs = new LinkedHashSet<>();
      this.optionalLibs = new LinkedHashSet<>();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException {
      return FileVisitResult.CONTINUE;
    }

    static String removeExtension(String s) {

      String separator = System.getProperty("file.separator");
      String filename;

      int lastSeparatorIndex = s.lastIndexOf(separator);
      if (lastSeparatorIndex == -1) {
        filename = s;
      } else {
        filename = s.substring(lastSeparatorIndex + 1);
      }

      // Remove the extension.
      int extensionIndex = filename.lastIndexOf(".");
      if (extensionIndex == -1) return filename;

      return filename.substring(0, extensionIndex);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

      val name = file.getFileName().toString();

      if (whitelist.contains(name + ".dll") || whitelist.contains(".dll.so")) {
        System.out.format("Not processing file %s\n", name);
        return FileVisitResult.CONTINUE;
      }

      if (!file.toString().endsWith(".dll.so")) {
        System.out.println("Skipping " + file);
        if (!whitelist.contains(file.getFileName().toString())) {
          optionalLibs.add(file);
        }
        return FileVisitResult.CONTINUE;
      }

      if (runTest(file)) {
        optionalLibs.add(file);
      } else {
        System.out.println("REQUIRED: " + file);
        requiredLibs.add(file);
        requiredLibs.add(file.getParent().resolve(Paths.get(removeExtension(file.toString()))));
      }
      return FileVisitResult.CONTINUE;
    }

    private boolean runTest(Path file) {
      try {
        val testExecutable = copyTargetBinary();
        val testResult = doRunTest(testExecutable);
        val tempName = renameFile(file);
        try {
          val secondTestResult = doRunTest(testExecutable);
          return expected(testResult, secondTestResult);
        } finally {
          restore(file, tempName);
        }
      } catch (Exception ex) {
        System.out.println("Got exception: " + ex.getMessage());
        ex.printStackTrace();
      }
      return false;
    }

    private boolean expected(TestResult testResult, TestResult secondTestResult) {
      return testResult.value == secondTestResult.value && testResult.value != 139;
    }

    private Path renameFile(Path file) throws IOException {
      System.out.format("Moving file %s -> %s.tmp\n", file, file);
      val newPath = Paths.get("temp.tmp");
      //      val newPath = Paths.get(file.toAbsolutePath() + ".tmp");
      //      val newPath = Paths.get("dlltemps").resolve(file);
      //      if (!Files.exists(Paths.get("dlltemps"))) {
      //        Files.createDirectories(Paths.get("dlltemps"));
      //      }
      Files.move(file, newPath);
      System.out.format("Successfully moved file %s -> %s.tmp\n", file, file);
      doSleep();
      return newPath;
    }

    void doSleep() {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {

      }
    }

    private void restore(Path file, Path tempName) throws IOException {
      System.out.format("Restoring file %s -> %s\n", tempName, file);
      Files.move(tempName, file);
      doSleep();
      System.out.format("Successfully restored file %s -> %s\n", tempName, file);
    }

    private TestResult doRunTest(File testExecutable) throws IOException, InterruptedException {

      System.out.format("Testing with %s\n", testExecutable);

      val processBuilder =
          new ProcessBuilder(
                  "tools/winewrapper", "rcedit-x64.exe", "rcedit-x86.exe", "--set-icon", iconName)
              .inheritIO();
      processBuilder.directory(new File(System.getProperty("user.dir")));

      val process = processBuilder.start();
      int result = process.waitFor();
      System.out.format("Process exited with status %s\n", result);

      return new TestResult(result);
    }

    private File copyTargetBinary() throws IOException {
      val file = new File(System.getProperty("user.dir"), targetBinary);
      val testFile = new File(System.getProperty("user.dir"), "test.exe");
      if (!file.exists()) {
        throw new IllegalStateException(format("Error: Target binary %s does not exist", file));
      }

      try (val sourceChannel = new FileInputStream(file).getChannel();
          val targetChannel = new FileOutputStream(testFile).getChannel()) {
        targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        if (!testFile.setExecutable(true)) {
          System.out.format("Failed to make file %s executable\n", testFile.getAbsolutePath());
        }
        return testFile;
      }
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
    }
  }
}
