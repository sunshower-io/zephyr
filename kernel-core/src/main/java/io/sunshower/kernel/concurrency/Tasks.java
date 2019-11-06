package io.sunshower.kernel.concurrency;

public class Tasks {

  public static ProcessBuilder newProcess(String name) {
    return new ProcessBuilder(name);
  }
}
