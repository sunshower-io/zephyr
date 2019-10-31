package io.sunshower.kernel.module;

import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Processor;

public class ModuleEntryWriteProcessor implements Processor {

  @Override
  public String getChannel() {
    return ModuleEntryWrite.channel;
  }

  public static final Processor instance = new ModuleEntryWriteProcessor();

  public static Processor getInstance() {
    return instance;
  }

  @Override
  public void process(ConcurrentProcess process) {
    process.perform();
  }
}
