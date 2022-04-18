package io.zephyr.kernel.core;

import io.zephyr.kernel.core.KernelPackageReexportConstraintSetProvider.Mode;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.val;

@Log
final class ModulePackageConstraintSet {

  static volatile ModulePackageConstraintSet INSTANCE;
  private final Set<String> exactAllowedPackages;
  private final Set<String> defaultPackages;
  private final Set<String> suffixInclusions;
  private final Set<String> suffixExclusions;
  private final Set<String> exactDeniedPackages;
  private final ClassLoader classLoader;

  ModulePackageConstraintSet(@NonNull ClassLoader classLoader) {
    defaultPackages =
        Set.of(
            "io.zephyr.kernel.core",
            "io.zephyr.kernel",
            "io.zephyr.api",
            "io.sunshower.lang.events");

    this.classLoader = classLoader;
    suffixInclusions = new HashSet<>();
    suffixExclusions = new HashSet<>();
    exactDeniedPackages = new HashSet<>();
    exactAllowedPackages = new HashSet<>(defaultPackages);
    computePackages();
  }

  private static List<KernelPackageReexportConstraintSetProvider> loadProviders(
      ClassLoader classLoader) {
    return ServiceLoader.load(KernelPackageReexportConstraintSetProvider.class, classLoader)
        .stream()
        .map(Provider::get)
        .sorted()
        .collect(Collectors.toUnmodifiableList());
  }

  static String deglob(String pkg) {
    return pkg.substring(0, pkg.lastIndexOf(".*"));
  }

  static ModulePackageConstraintSet getInstance(ClassLoader classLoader) {
    var instance = INSTANCE;
    if (instance == null) {
      synchronized (ModulePackageConstraintSet.class) {
        instance = INSTANCE;
        if (instance == null) {
          instance = INSTANCE = new ModulePackageConstraintSet(classLoader);
        }
      }
    }
    return instance;
  }

  static boolean canReexportPackage(String name, ClassLoader classLoader) {
    return getInstance(classLoader).canReexport(name);
  }

  private void computePackages() {
    log.log(Level.INFO, "Loading reexported package definitions...");
    val providers = loadProviders(classLoader);
    for (val provider : providers) {
      partitionIncludes(provider, provider.getMode());
    }
  }

  private void partitionIncludes(KernelPackageReexportConstraintSetProvider provider, Mode mode) {
    for (val pkg : provider.getPackages()) {
      val glob = isGlob(pkg);
      if (glob) {
        if (mode == Mode.Include) {
          suffixInclusions.add(deglob(pkg));
        } else {
          suffixExclusions.add(deglob(pkg));
        }
      } else {
        if (mode == Mode.Include) {
          exactAllowedPackages.add(pkg);
        } else {
          exactDeniedPackages.add(pkg);
        }
      }
    }
    logPackages();
  }

  private boolean isGlob(String pkg) {
    return pkg.endsWith(".*");
  }

  private void logPackages() {
    if (log.isLoggable(Level.INFO)) {
      logSet("Computed allowed exact package reexports: ", exactAllowedPackages);
      logSet("Computed allowed suffix glob package reexports: ", suffixInclusions);
      logSet("Computed denied suffix glob package reexports: ", suffixExclusions);
    }
  }

  private void logSet(String msg, Set<String> packages) {
    log.log(Level.INFO, msg);
    for (val pkg : packages) {
      log.log(Level.INFO, "\t{0}", pkg);
    }
  }

  boolean canReexport(String name) {
    boolean reexported =
        name.startsWith("java")
            || name.startsWith("com.sun")
            || name.startsWith("javax")
            || name.startsWith("org.w3c")
            || name.startsWith("jdk")
            || name.startsWith("sun")
            || name.startsWith("org.ietf")
            || name.startsWith("org.xml")
            || exactAllowedPackages.contains(name);
    if (reexported && !exactDeniedPackages.contains(name)) {
      return true;
    }
    return included(name) && !excluded(name);
  }

  private boolean excluded(String name) {
    for (val pkg : suffixExclusions) {
      if (name.startsWith(pkg)) {
        return true;
      }
    }
    return false;
  }

  private boolean included(String name) {
    for (val pkg : suffixInclusions) {
      if (name.startsWith(pkg)) {
        return true;
      }
    }
    return false;
  }
}
