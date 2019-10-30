package io.sunshower.kernel.concurrency;

public interface ConcurrentProcess {
  String getChannel();

  void perform();
}
