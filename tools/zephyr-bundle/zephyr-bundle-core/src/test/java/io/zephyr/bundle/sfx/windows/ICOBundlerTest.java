package io.zephyr.bundle.sfx.windows;

import static io.zephyr.bundle.sfx.formats.ICO.resize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.test.common.Tests;
import io.zephyr.bundle.sfx.icons.IconDefinition;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.val;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.junit.jupiter.api.Test;

public class ICOBundlerTest {

  @Test
  void ensureBundlingPNGIconIntoAllAvailableFormatsWorks() throws IOException {
    val file = resolveIcon("icons/installer256px.png");
    val icons = resize(file, IconDefinition.Size.all());
    val tempFile = new File(Tests.buildDirectory(), "test.ico");
    tempFile.deleteOnExit();
    ICOEncoder.write(icons, tempFile);
    val ico = ICODecoder.read(tempFile);
    assertEquals(ico.size(), IconDefinition.Size.all().size());
  }

  @Test
  void ensureBundlingSVGIconIntoAllAvailableFormatsWorks() throws IOException {
    val file = resolveIcon("icons/installer.svg");
    val icons = resize(file, IconDefinition.Size.all());
    val tempFile = new File(Tests.buildDirectory(), "test-svg.ico");
    ICOEncoder.write(icons, tempFile);
    tempFile.deleteOnExit();
    val ico = ICODecoder.read(tempFile);
    assertEquals(ico.size(), IconDefinition.Size.all().size());
  }

  static BufferedImage resolveIcon(String path) {
    try {
      return ImageIO.read(ClassLoader.getSystemResource(path));
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
