package io.zephyr.kernel.memento;

import java.io.File;

public interface MementoFactory<T> {

  boolean canSave(File file);

  Memento<T> createMemento(File file, Class<T> item);
}
