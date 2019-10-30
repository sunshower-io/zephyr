package io.sunshower.kernel.module;

import io.sunshower.kernel.core.KernelException;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import lombok.val;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.AvoidLiteralsInIfCondition"
})
public class ModuleListParser {

  static final Logger log = Logging.get(ModuleListParser.class);

  static final String FILE_SYSTEM_URI = "droplet://kernel";

  static final int BUFFER_SIZE = 8192;
  static final String lineSeparator = System.getProperty("line.separator");
  static final int newLineLength = lineSeparator.length();

  public static List<KernelModuleEntry> read(FileSystem fs, String moduleList) {
    val file = resolveModuleFile(fs, moduleList);
    val results = new ArrayList<KernelModuleEntry>();
    read(file, results);
    return results;
  }

  @SuppressFBWarnings
  static void read(File file, List<KernelModuleEntry> kernelModuleEntries) {

    try (val channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
      val inputStream = new PushbackInputStream(Channels.newInputStream(channel));
      doParse(inputStream, kernelModuleEntries, new Position());
    } catch (IOException e) {
      throw new ModuleListSyntaxException(e);
    }
  }

  static void doParse(
      PushbackInputStream inputStream, List<KernelModuleEntry> kernelModuleEntries, Position pos)
      throws IOException {
    for (; ; ) {
      var pint = peekInt(inputStream);
      if (pint == -1 || pint == 255) {
        return;
      }

      parseModuleList(inputStream, kernelModuleEntries, pos);
      inputStream.read();
      trimWhitespace(inputStream, pos);
      pint = peekInt(inputStream);
      if (pint == -1 || pint == 255) {
        return;
      }
    }
  }

  private static void parseModuleList(
      PushbackInputStream inputStream, List<KernelModuleEntry> kernelModuleEntries, Position pos)
      throws IOException {
    val order = parseOrder(inputStream, pos);
    expect(inputStream, ":", pos);
    val group = readUntil(inputStream, pos, ":");
    expect(inputStream, ":", pos);
    val name = readUntil(inputStream, pos, ":");
    expect(inputStream, ":", pos);
    val version = readUntil(inputStream, pos, "[".concat(lineSeparator));
    final List<String> directories;
    if (peek(inputStream) == '[') {
      directories = new ArrayList<>();
      readLibraryFiles(inputStream, pos, directories);
    } else {
      directories = Collections.emptyList();
    }
    kernelModuleEntries.add(new KernelModuleEntry(order, name, group, version, directories));
  }

  private static void readLibraryFiles(
      PushbackInputStream inputStream, Position pos, List<String> directories) throws IOException {
    expect(inputStream, "[", pos);
    for (; ; ) {
      directories.add(readUntil(inputStream, pos, ",]"));
      if (peek(inputStream) == ']') {
        break;
      }
      expect(inputStream, ",", pos);
    }
    //    while (peek(inputStream) != ']') {
    //      directories.add(readUntil(inputStream, pos, ","));
    //    }
  }

  static String readUntil(PushbackInputStream inputStream, Position pos, String chars)
      throws IOException {
    val chs = new StringBuilder();
    for (; ; ) {
      int ch = inputStream.read();

      for (int i = 0; i < chars.length(); i++) {
        char c = chars.charAt(i);
        if (ch == c || ch == -1) {
          if (chs.length() == 0) {
            throw new ModuleListSyntaxException();
          }
          inputStream.unread(ch);
          pos.pos--;
          return chs.toString();
        }
      }
      chs.append((char) ch);
      pos.pos++;
    }
  }

  static void expect(PushbackInputStream inputStream, String c, Position pos) throws IOException {
    char ch = peek(inputStream);
    for (int i = 0; i < c.length(); i++) {
      if (ch == c.charAt(i)) {
        inputStream.read();
        return;
      }
    }
    throw new ModuleListSyntaxException();
  }

  static int peekInt(PushbackInputStream inputStream) throws IOException {
    int ch = inputStream.read();
    inputStream.unread(ch);
    return ch;
  }

  static char peek(PushbackInputStream inputStream) throws IOException {
    int ch = inputStream.read();
    inputStream.unread(ch);
    return (char) ch;
  }

  static int parseOrder(PushbackInputStream inputStream, Position pos) throws IOException {
    trimWhitespace(inputStream, pos);
    val chs = new StringBuilder();
    for (; ; ) {
      int ch = inputStream.read();
      if (ch == -1 || ch == ':' || isEOL(ch, inputStream)) {
        if (chs.length() == 0) {
          throw new ModuleListSyntaxException();
        }
        inputStream.unread(ch);
        pos.pos--;
        return Integer.parseInt(chs.toString());
      } else {
        pos.pos++;
        chs.append((char) ch);
      }
    }
  }

  /**
   * Consumes the newline if it exists and positions stream at the next position, else leaves the
   */
  static boolean isEOL(int ch, PushbackInputStream inputStream) throws IOException {
    if (newLineLength == 1 && ch == '\n') {
      return true;
    }
    if (ch == '\r') {
      val next = inputStream.read();
      if (next == '\n') {
        return true;
      } else {
        throw new ModuleListSyntaxException();
      }
    }
    return false;
  }

  static void trimWhitespace(PushbackInputStream inputStream, Position pos) throws IOException {
    for (; ; ) {
      val ch = inputStream.read();
      if (!Character.isWhitespace(ch)) {
        inputStream.unread(ch);
        return;
      }
      pos.pos++;
    }
  }

  static File resolveModuleFile(FileSystem fs, String modFile) {
    val path = fs.getPath(modFile);
    val file = path.toFile();
    if (!file.exists()) {
      log.log(Level.INFO, "module.file.create.attempting", "droplet://kernel/module.list", path);
      try {
        if (!file.createNewFile()) {
          log.log(Level.WARNING, "module.file.create.failed", "droplet://kernel/module.list", path);
        } else {
          log.log(Level.INFO, "module.file.create.succeeded", "droplet://kernel/module.list", path);
        }
      } catch (IOException ex) {
        throw new KernelException(
            "Failed to create file and it does not exist--cannot continue", ex);
      }
    }
    return file.getAbsoluteFile();
  }

  static class Position {
    int pos;
  }
}
