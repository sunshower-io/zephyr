package io.zephyr.cli;

public interface BuilderWithKernelThreads extends Creator {
    BuilderWithUserThreads maxUserThreads(int userThreads);
}
