package io.zephyr.bundle.sfx.icons;

import io.zephyr.bundle.sfx.Log;
import java.io.File;
import java.nio.file.Path;

public interface ImageBundler {

  File bundle(CompositeIconDefinition definition, Path tempFile, Log log);
}
