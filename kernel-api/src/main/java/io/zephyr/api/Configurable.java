package io.zephyr.api;

public interface Configurable<T extends Configuration> {

  T getConfiguration();

  void setConfiguration(T configuration);

  void updateConfiguration(T configuration);

  void save();

  void restore(Configuration configuration);
}
