package io.zephyr.kernel.classloading;

import io.sunshower.checks.SuppressFBWarnings;
import io.zephyr.kernel.KernelModuleEntry;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import lombok.val;

@SuppressFBWarnings
@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.EmptyCatchBlock"
})
public final class KernelClassloader extends URLClassLoader {

  static final int BUFFER_SIZE = 1024;

  final Object lock = new Object();
  private final List<KernelModuleEntry> kernelModules;

  public KernelClassloader(URL[] urls, ClassLoader parent, List<KernelModuleEntry> entries) {
    super(urls, parent);
    this.kernelModules = entries;
  }

  @Override
  public URL findResource(String name) {
    val resource = super.findResource(name);
    if (resource == null) {
      val checkUrl = "WEB-INF/classes/" + name;
      for (URL url : getURLs()) {
        if (url.toString().endsWith(".droplet")) {
          try {
            val test = new URL("jar:" + url + "!/" + checkUrl);
            try (val t = test.openStream()) {

            } catch (IOException ex) {
              continue;
            }
            return test;

          } catch (MalformedURLException ex) {
            return null;
          }
        }
      }
    }
    return null;
  }

  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    return super.findResources(name);
  }

  @SuppressFBWarnings
  @SuppressWarnings("PMD.SystemPrintln")
  private Class<?> searchInDroplets(String name) throws IOException, ClassNotFoundException {
    val path = "WEB-INF/classes/" + name.replace('.', '/') + ".class";

    for (val url : getURLs()) {
      if (url.toString().endsWith(".droplet")) {
        val classUrl = new URL("jar:" + url + "!/" + path);
        try (val input = classUrl.openStream();
            val output = new ByteArrayOutputStream()) {

          byte[] data = new byte[BUFFER_SIZE];
          int read;
          for (; ; ) {
            read = input.read(data, 0, data.length);
            if (read == -1) break;
            output.write(data, 0, read);
          }
          byte[] classdata = output.toByteArray();
          return defineClass(name, classdata, 0, classdata.length);
        } catch (FileNotFoundException ex) {
          continue;
        }
      }
    }
    throw new ClassNotFoundException(name);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    try {
      return super.findClass(name);
    } catch (ClassNotFoundException e) {
      // eh
    }
    try {
      return searchInDroplets(name);
    } catch (IOException | ClassNotFoundException e) {
      throw new ClassNotFoundException(name, e);
    }
  }

  public List<KernelModuleEntry> getKernelModules() {
    return kernelModules;
  }
}
