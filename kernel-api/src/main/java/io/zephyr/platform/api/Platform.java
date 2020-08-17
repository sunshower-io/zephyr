package io.zephyr.platform.api;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.val;

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

  public static OperatingSystem current() {
    return EnumSet.allOf(OperatingSystem.class).stream()
        .filter(OperatingSystem::is)
        .findAny()
        .orElseThrow(UnknownPlatformException::new);
  }

  public static <T> Optional<T> resolveService(Class<T> service, ClassLoader classLoader) {
    return ServiceLoader.load(service, classLoader).findFirst();
  }

  public static <T extends NativeService> T demandNativeService(
      ClassLoader classLoader, Class<T> type) {
    val current = current();
    return demandService(classLoader, type, t -> t.isNativeTo(current) || t.canRunOn(current));
  }

  public static <T> T demandService(ClassLoader loader, Class<T> type, Predicate<T> predicate) {
    return ServiceLoader.load(type, loader).stream()
        .map(ServiceLoader.Provider::get)
        .filter(predicate)
        .findAny()
        .orElseThrow(NoSuchElementException::new);
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
        .filter(filter)
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
