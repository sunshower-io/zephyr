package io.zephyr.kernel.core;

import io.zephyr.kernel.core.KernelPackageReexportConstraintSetProvider.Mode;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.java.Log;
import lombok.val;
import org.jboss.modules.LocalLoader;
import org.jboss.modules.Resource;

@Log
public class KernelClasspathLocalLoader implements LocalLoader {

  static final Set<String> ZEPHYR_PACKAGES;

  static {
    val DEFAULT_PACKAGES = Set.of(
        "io.zephyr.kernel.core", "io.zephyr.kernel", "io.zephyr.api", "io.zephyr.kernel.events"
    );
    ZEPHYR_PACKAGES =
        Stream.concat(DEFAULT_PACKAGES.stream(), getReexportedPackages().stream()).collect(
            Collectors.toUnmodifiableSet());

  }

  //    final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
  final ClassLoader classLoader;

  public KernelClasspathLocalLoader(final Kernel kernel) {
    this.classLoader = kernel.getClassLoader();
  }

  private static Set<String> getReexportedPackages() {
    log.log(Level.INFO, "Loading reexported package definitions...");
    val result = new HashSet<String>();
    val providers = loadProviders();

    for (val provider : providers) {
      if (provider.getMode() == Mode.Include) {
        result.addAll(provider.getPackages());
      } else {
        result.removeAll(provider.getPackages());
      }
    }
    logPackages(result);
    return result;
  }

  private static void logPackages(HashSet<String> result) {
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "Computed allowed package reexports: ");
      for (val pkg : result) {
        log.log(Level.INFO, "\t{0}", pkg);
      }
    }
  }

  private static List<KernelPackageReexportConstraintSetProvider> loadProviders() {
    return ServiceLoader.load(KernelPackageReexportConstraintSetProvider.class,
            Thread.currentThread()
                .getContextClassLoader()).stream()
        .map(Provider::get)
        .sorted().toList();
  }

  @Override
  public Class<?> loadClassLocal(String name, boolean resolve) {
    try {
      val type = Class.forName(name, true, classLoader);
      if (canReexport(type.getPackageName())) {
        return type;
      } else {
        log.log(
            Level.INFO,
            String.format(
                "Found, but not loading external class ('%s')--not in Zephyr public API", name));
        return null;
      }
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  public Package loadPackageLocal(String name) {
    return classLoader.getDefinedPackage(name);
  }

  @Override
  public List<Resource> loadResourceLocal(String name) {
    return Collections.emptyList();
  }

  private boolean canReexport(String name) {
    return name.startsWith("java")
           || name.startsWith("com.sun")
           || name.startsWith("javax")
           || name.startsWith("org.w3c")
           || name.startsWith("jdk")
           || name.startsWith("org.ietf")
           || name.startsWith("org.xml")
           || ZEPHYR_PACKAGES.contains(name);
  }
}
