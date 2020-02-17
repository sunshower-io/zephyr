package io.zephyr.cli;

public interface BuilderWithHomeDirectory extends Creator {
  BuilderWithKernelThreads maxKernelThreads(int threads);
}
