package io.sunshower.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;
import lombok.val;
import org.jboss.modules.ModuleClassLoader;
import org.jetbrains.annotations.Nullable;

public class WeakReferenceClassLoader extends ClassLoader {
  final WeakReference<ModuleClassLoader> classloader;

  public WeakReferenceClassLoader(ModuleClassLoader classLoader) {
    this.classloader = new WeakReference<>(classLoader);
  }

  private ClassLoader check() {
    val cl = classloader.get();
    if (cl == null) {
      throw new IllegalStateException("Classloader has been unloaded");
    }
    return cl;
  }

  @Override
  public String getName() {
    if (classloader == null) {
      return "WeakRefClassLoader::unloaded";
    }
    return check().getName();
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return check().loadClass(name);
  }

  @Nullable
  @Override
  public URL getResource(String name) {
    return check().getResource(name);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    return check().getResources(name);
  }

  @Override
  public Stream<URL> resources(String name) {
    return check().resources(name);
  }

  @Nullable
  @Override
  public InputStream getResourceAsStream(String name) {
    return check().getResourceAsStream(name);
  }

  @Override
  public void setDefaultAssertionStatus(boolean enabled) {
    check().setDefaultAssertionStatus(enabled);
  }

  @Override
  public void setPackageAssertionStatus(String packageName, boolean enabled) {
    check().setPackageAssertionStatus(packageName, enabled);
  }

  @Override
  public void setClassAssertionStatus(String className, boolean enabled) {
    check().setClassAssertionStatus(className, enabled);
  }

  @Override
  public void clearAssertionStatus() {
    check().clearAssertionStatus();
  }
}
