package io.zephyr.api;

public interface ServiceRegistration<T> extends Disposable {
  ServiceReference<T> getReference();
}
