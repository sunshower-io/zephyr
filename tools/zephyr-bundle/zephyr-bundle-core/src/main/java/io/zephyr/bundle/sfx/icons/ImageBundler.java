package io.zephyr.bundle.sfx.icons;

import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;
import java.io.File;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import lombok.val;

public interface ImageBundler {

  static ImageBundler resolve(CompositeIconDefinition.Format ico, ClassLoader classLoader) {
    val result = ServiceLoader.load(ImageBundler.class, classLoader);
    for (val loader : result) {
      if (loader.supports(ico)) {
        return loader;
      }
    }
    throw new NoSuchElementException("Error: no service loader applicable to format: " + ico);
  }

  boolean supports(CompositeIconDefinition.Format format);

  File bundle(SelfExtractingExecutableConfiguration definition, Path tempFile, Log log);
}
