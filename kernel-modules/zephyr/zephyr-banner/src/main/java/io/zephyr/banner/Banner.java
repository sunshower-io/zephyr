package io.zephyr.banner;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.modules.shell.command.ColoredConsole;
import io.zephyr.kernel.modules.shell.console.Color;
import io.zephyr.kernel.modules.shell.console.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public final class Banner implements EntryPoint {
  static final Logger log = Logger.getLogger(Banner.class.getName());

  final Console console = new ColoredConsole();

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public Options<?> getOptions() {
    return Options.EMPTY;
  }

  @Override
  public int getPriority() {
    return HIGHEST_PRIORITY + 5;
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    try {
      print(System.out);
    } catch (IOException ex) {
      log.log(Level.INFO, "failed to print banner.  Reason {0}", ex.getMessage());
    }
  }

  void print(PrintStream out) throws IOException {
    val url = Banner.class.getClassLoader().getResource("assets/banner.txt");
    if (url == null) {
      throw new IllegalStateException("No banner file");
    }
    String result = load(url);
    out.println(result);
  }

  void print() throws IOException {
    val url = Banner.class.getClassLoader().getResource("assets/banner.txt");
    if (url == null) {
      throw new IllegalStateException("No banner file");
    }
    String result = load(url);
    console.writeln(result, Color.colors(Color.BlueBold));
  }

  String load(URL url) throws IOException {
    try (val stream = url.openStream()) {
      val scanner = new Scanner(stream).useDelimiter("\\A");
      val value = scanner.next();
      return format(value);
    }
  }

  String format(String value) throws IOException {
    val url = Banner.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
    try (val stream = url.openStream()) {
      val manifest = new Manifest(stream);
      val attributes = manifest.getMainAttributes();
      val version = attributes.getValue("Kernel-Version");
      val jdk = attributes.getValue("Build-Jdk");
      val buildRevision = attributes.getValue("Build-Revision");
      return MessageFormat.format(value, version, jdk, buildRevision);
    }
  }
}
