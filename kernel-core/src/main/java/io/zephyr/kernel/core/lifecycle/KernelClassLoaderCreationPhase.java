package io.zephyr.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.classloading.KernelClassloader;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.module.KernelModuleEntry;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.val;

@SuppressFBWarnings
public class KernelClassLoaderCreationPhase extends Task {

  public static final String INSTALLED_KERNEL_CLASSLOADER = "INSTALLED_KERNEL_CLASSLOADER";

  public KernelClassLoaderCreationPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    List<KernelModuleEntry> entries = scope.get(KernelModuleListReadPhase.INSTALLED_MODULE_LIST);
    val kernel = scope.<SunshowerKernel>get("SunshowerKernel");

    try {
      URL[] url = readUrls(entries);
      val loader = new KernelClassloader(url, ClassLoader.getSystemClassLoader());
      kernel.setClassLoader(loader);
    } catch (MalformedURLException ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }

  @SuppressFBWarnings
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private URL[] readUrls(List<KernelModuleEntry> entries) throws MalformedURLException {
    final Set<URL> urls = new HashSet<>();

    for (val entry : entries) {

      for (val libFile : entry.getLibraryFiles()) {
        val f = new File(libFile);
        urls.add(f.toURI().toURL());
      }
    }
    return urls.toArray(new URL[0]);
  }
}
