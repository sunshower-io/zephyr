package io.sunshower.kernel.ext;

import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.memento.Memento;

import java.io.InputStream;
import java.io.OutputStream;

public class ModuleManagerMemento implements Memento<ModuleManager> {

  @Override
  public void read(InputStream inputStream) {


  }

  @Override
  public void write(OutputStream outputStream) {}
}
