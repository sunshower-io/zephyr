package io.zephyr.bundle.sfx.windows;

import static java.lang.String.format;

import io.zephyr.bundle.sfx.IOUtilities;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;
import io.zephyr.bundle.sfx.formats.ICO;
import io.zephyr.bundle.sfx.icons.CompositeIconDefinition;
import io.zephyr.bundle.sfx.icons.IconDefinition;
import io.zephyr.bundle.sfx.icons.ImageBundler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.val;
import net.sf.image4j.codec.ico.ICOEncoder;

public class ICOBundler implements ImageBundler {

  @Override
  public boolean supports(CompositeIconDefinition.Format format) {
    return format == CompositeIconDefinition.Format.ICO;
  }

  @Override
  public File bundle(SelfExtractingExecutableConfiguration definition, Path tempFile, Log log) {
    val excfg = definition.getExecutableFileConfiguration();
    val iconDefinition = excfg.getIconDefinition();
    val sourceFile = iconDefinition.getSource();

    if (sourceFile == null) {
      log.warn("Error: image source file must not be null");
      throw new IllegalArgumentException("image source file must not be null");
    }
    log.info(
        "Attempting to generate ICO file from '%s' in sizes [\n%s\n]...",
        sourceFile, getSizes(iconDefinition));

    IOUtilities.checkFile(sourceFile.toPath(), log);
    val image = readImage(sourceFile, log);
    val icons = ICO.resize(image, getSizeValues(iconDefinition), log);

    val actualTempFile = tempFile.resolve(UUID.randomUUID().toString() + ".ico").toFile();
    try {
      if (!actualTempFile.exists()) {
        val parent = actualTempFile.getParentFile();
        if (!(parent.exists() || parent.mkdirs())) {
          throw new IllegalArgumentException(
              format(
                  "Error: failed to create '%s'--do you have appropriate permissions in its parent directory ('%s')?",
                  parent, parent.getParent()));
        }
        if (!actualTempFile.createNewFile()) {
          throw new IllegalArgumentException(
              format(
                  "Error: failed to create '%s'--do you have appropriate permissions in its parent directory ('%s')?",
                  actualTempFile, actualTempFile.getParent()));
        }
      }
    } catch (IOException e) {
      log.warn("Failed to create file '%s', reason: '%s'", actualTempFile, e.getMessage());
      throw new IllegalArgumentException(e);
    }

    try {
      log.info("Writing intermediate file to '%s'...", actualTempFile);
      ICOEncoder.write(icons, actualTempFile);
    } catch (IOException ex) {
      log.warn(
          "Failed to save intermediate file '%s', reason: %s", actualTempFile, ex.getMessage());
    }
    return actualTempFile.getAbsoluteFile();
  }

  private BufferedImage readImage(File sourceFile, Log log) {
    try {
      log.info("Attempting to read image from '%s'", sourceFile);
      val result = ImageIO.read(sourceFile);
      log.info("Successfully read image from '%s'", sourceFile);
      return result;
    } catch (IOException ex) {
      log.warn(
          "Encountered a fatal error (%s) while attempting to read '%s'.  Can't continue",
          ex.getMessage(), sourceFile);
      throw new IllegalArgumentException(ex);
    }
  }

  private List<IconDefinition.Size> getSizeValues(CompositeIconDefinition iconDefinition) {
    val results = new ArrayList<IconDefinition.Size>();

    for (val size : iconDefinition.getIconDefinitions()) {
      results.add(size.getSize());
    }
    return results;
  }

  private String getSizes(CompositeIconDefinition iconDefinition) {
    val definitions = iconDefinition.getIconDefinitions();
    val result = new StringBuilder();
    for (val definition : definitions) {
      val size = definition.getSize();
      result.append(
          format(
              "\twidth: %s, height: %s, format: %s\n",
              size.getWidth(), size.getHeight(), definition.getChannel()));
    }
    return result.toString();
  }
}
