package io.zephyr.breeze;

public interface Instantiator {

  <T> T create(Class<T> type);
}
