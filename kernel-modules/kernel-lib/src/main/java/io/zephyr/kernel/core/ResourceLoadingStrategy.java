package io.zephyr.kernel.core;

import org.jboss.modules.ResourceLoader;

public interface ResourceLoadingStrategy {

  ResourceLoader resourceLoader(ClassLoader classLoader);
}
