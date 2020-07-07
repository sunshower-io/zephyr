package io.zephyr.bundle.sfx.icons;

import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;
import java.io.File;
import java.nio.file.Path;

public interface ImageBundler {

  boolean supports(CompositeIconDefinition.Format format);

  File bundle(SelfExtractingExecutableConfiguration definition, Path tempFile, Log log);
}
