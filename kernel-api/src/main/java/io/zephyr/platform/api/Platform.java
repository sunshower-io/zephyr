package io.zephyr.platform.api;

import lombok.val;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Platform {

  static final String CURRENT = System.getProperty("os.name");

  enum OperatingSystem {
    Linux("Linux", "LINUX", "linux"),
    OSX("Mac OS X"),
    Windows("Windows");

    final String[] prefixes;

    OperatingSystem(String... prefixes) {
      this.prefixes = prefixes;
    }

    public static boolean matches(String name, String prefix) {
      return name != null && name.startsWith(prefix);
    }

    public static boolean is(OperatingSystem os) {
      for (val prefix : os.prefixes) {
        if (matches(CURRENT, prefix)) {
          return true;
        }
      }
      return false;
    }
  }

  private Platform() {
    throw new IllegalStateException("No platforms for you!");
  }

  public static <T> Optional<T> resolveService(Class<T> service, ClassLoader classLoader) {
    return ServiceLoader.load(service, classLoader).findFirst();
  }

  public static <T> T demandService(
      Class<T> service, ClassLoader classLoader, Predicate<ServiceLoader.Provider<T>> filter) {
    return ServiceLoader.load(service, classLoader).stream()
        .filter(filter)
        .findAny()
        .map(ServiceLoader.Provider::get)
        .orElseThrow(NoSuchElementException::new);
  }

  public static <T> Collection<ServiceLoader.Provider<T>> resolveProviders(
      Class<T> service, ClassLoader classLoader, Predicate<ServiceLoader.Provider<T>> filter) {
    return ServiceLoader.load(service, classLoader).stream()
        .filter(filter::test)
        .collect(Collectors.toSet());
  }

  public static <T> Collection<T> resolveServices(
      Class<T> service, ClassLoader classLoader, Predicate<T> filter) {
    return ServiceLoader.load(service, classLoader).stream()
        .map(ServiceLoader.Provider::get)
        .filter(filter)
        .collect(Collectors.toSet());
  }
}
