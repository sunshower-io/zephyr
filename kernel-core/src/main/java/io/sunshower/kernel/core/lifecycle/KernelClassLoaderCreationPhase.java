package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.classloading.KernelClassloader;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.module.KernelModuleEntry;
import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;
import io.sunshower.kernel.process.Process;
import io.sunshower.kernel.status.Status;
import io.sunshower.kernel.status.StatusType;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.val;

public class KernelClassLoaderCreationPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  enum EventType implements KernelProcessEvent {}

  public static final String INSTALLED_KERNEL_CLASSLOADER = "INSTALLED_KERNEL_CLASSLOADER";

  public KernelClassLoaderCreationPhase() {
    super(EventType.class);
  }

  @Override
  @SuppressFBWarnings // nobody effing deploys policies to containers
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {

    List<KernelModuleEntry> entries =
        context.getContextValue(KernelModuleListReadPhase.INSTALLED_MODULE_LIST);

    try {
      URL[] url = readUrls(entries);
      val loader = new KernelClassloader(url, ClassLoader.getSystemClassLoader());
      ((SunshowerKernel) context.getKernel()).setClassLoader(loader);
    } catch (MalformedURLException ex) {
      val status = new Status(StatusType.FAILED, "Module list corrupted", false);
      process.addStatus(status);
      throw status.toException();
    }
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
