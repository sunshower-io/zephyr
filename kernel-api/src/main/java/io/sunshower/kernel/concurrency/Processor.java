package io.sunshower.kernel.concurrency;

public interface Processor {
  String getChannel();

  void process(ConcurrentProcess process);
}
