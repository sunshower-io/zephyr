package io.sunshower.kernel.launch;

import lombok.val;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.jar.Manifest;

public final class Banner {

  public Banner() {}

  public void print(PrintStream out) throws IOException {
    val url = Banner.class.getClassLoader().getResource("assets/banner.txt");
    if (url == null) {
      throw new IllegalStateException("No banner file");
    }

    String result = load(url);
    out.println(result);
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
