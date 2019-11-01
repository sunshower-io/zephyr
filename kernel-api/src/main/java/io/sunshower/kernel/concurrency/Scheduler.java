package io.sunshower.kernel.concurrency;

import java.util.concurrent.CompletableFuture;

public interface Scheduler {

  void terminate();

  void start();

  boolean isRunning();

  void stop(String channel);

  void start(String channel);

  void awaitShutdown();

  boolean cancel(ConcurrentProcess action);

  void registerHandler(Processor processor);

  void unregisterHandler(Processor processor);

  CompletableFuture<Void> scheduleTask(ConcurrentProcess action);

  void await(String channel);

  void synchronize();
}
