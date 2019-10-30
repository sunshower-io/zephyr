package io.sunshower.kernel.concurrency;

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

  boolean scheduleTask(ConcurrentProcess action);
}
