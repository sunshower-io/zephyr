package io.zephyr.cli;

import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.NamedThreadFactory;
import io.zephyr.kernel.concurrency.WorkerPool;
import io.zephyr.kernel.core.DaggerSunshowerKernelConfiguration;
import io.zephyr.kernel.launch.KernelOptions;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.val;

@SuppressWarnings("PMD.UseProperClassLoader")
final class builders {

  static class Builder implements ZephyrBuilder {

    @Override
    public BuilderWithHomeDirectory homeDirectory(File file) {
      return new ZephyrBuilderWithHomeDirectory(file);
    }
  }

  @AllArgsConstructor
  static class ZephyrBuilderWithHomeDirectory implements BuilderWithHomeDirectory {
    final File homeDirectory;

    @Override
    public BuilderWithKernelThreads maxKernelThreads(int threads) {
      return new ZephyrBuilderWithKernelThreads(threads, homeDirectory);
    }

    @Override
    public Zephyr create(ClassLoader classLoader) {

      return builders.create(homeDirectory, classLoader);
    }
  }

  @AllArgsConstructor
  static class ZephyrBuilderWithKernelThreads implements BuilderWithKernelThreads {
    final int kernelThreads;
    final File homeDirectory;

    @Override
    public BuilderWithUserThreads maxUserThreads(int userThreads) {
      return new ZephyrBuilderWithUserThreads(kernelThreads, userThreads, homeDirectory);
    }

    @Override
    public Zephyr create(ClassLoader classLoader) {
      return builders.create(homeDirectory, classLoader);
    }
  }

  @AllArgsConstructor
  static class ZephyrBuilderWithUserThreads implements BuilderWithUserThreads, Creator {
    final int kernelThreads;
    final int userThreads;
    final File homeDirectory;

    @Override
    public Zephyr create(ClassLoader classLoader) {

      val kernel =
          DaggerSunshowerKernelConfiguration.factory()
              .create(options(), classLoader, workerPool())
              .kernel();
      return new DefaultZephyr(kernel);
    }

    private KernelOptions options() {
      val result = new KernelOptions();
      result.setHomeDirectory(homeDirectory);
      result.setConcurrency(userThreads);
      result.setKernelConcurrency(kernelThreads);
      return result;
    }
  }

  private static WorkerPool workerPool() {
    val kernelFactory = Executors.newCachedThreadPool(new NamedThreadFactory("kernel"));
    val userFactory = Executors.newCachedThreadPool(new NamedThreadFactory("module"));
    return new ExecutorWorkerPool(userFactory, kernelFactory);
  }

  static Zephyr create(File homeDirectory, ClassLoader classLoader) {
    val options = new KernelOptions();
    options.setHomeDirectory(homeDirectory);
    val kernel =
        DaggerSunshowerKernelConfiguration.factory()
            .create(options, classLoader, workerPool())
            .kernel();
    return new DefaultZephyr(kernel);
  }
}
