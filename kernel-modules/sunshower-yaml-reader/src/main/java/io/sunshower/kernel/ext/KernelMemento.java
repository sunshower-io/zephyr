package io.sunshower.kernel.ext;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.memento.Memento;
import java.io.InputStream;
import java.io.OutputStream;

public class KernelMemento implements Memento<Kernel> {

  @Override
  public void read(InputStream inputStream) {}

  @Override
  public void write(OutputStream outputStream) {}
}
