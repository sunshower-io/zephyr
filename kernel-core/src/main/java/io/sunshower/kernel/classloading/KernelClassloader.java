package io.sunshower.kernel.classloading;

import java.net.URL;
import java.net.URLClassLoader;

public class KernelClassloader extends URLClassLoader {

  public KernelClassloader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }
}
