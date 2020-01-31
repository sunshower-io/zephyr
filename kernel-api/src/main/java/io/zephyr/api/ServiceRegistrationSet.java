package io.zephyr.api;

import java.util.Collection;
import java.util.Iterator;

public interface ServiceRegistrationSet extends Iterable<ServiceRegistration<?>> {

  default Iterator<ServiceRegistration<?>> iterator() {
    return getRegistrations().iterator();
  }

  Collection<ServiceRegistration<?>> getRegistrations();
}
