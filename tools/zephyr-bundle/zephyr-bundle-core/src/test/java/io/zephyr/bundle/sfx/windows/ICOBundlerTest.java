package io.zephyr.bundle.sfx.windows;

import static io.zephyr.bundle.sfx.formats.ICO.resize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import io.sunshower.test.common.Tests;
import io.zephyr.bundle.sfx.ExecutableFileConfiguration;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;
import io.zephyr.bundle.sfx.icons.CompositeIconDefinition;
import io.zephyr.bundle.sfx.icons.IconDefinition;
import io.zephyr.bundle.sfx.icons.ImageBundler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import lombok.val;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.junit.jupiter.api.Test;

public class ICOBundlerTest {

  static final Log sysout =
      new Log() {

        @Override
        public Level getLevel() {
          return Level.ALL;
        }

        @Override
        public void warn(String message, Object... args) {
          System.out.format(message + "\n", args);
        }

        @Override
        public void debug(String message, Object... args) {
          System.out.format(message + "\n", args);
        }

        @Override
        public void info(String message, Object... args) {
          System.out.format(message + "\n", args);
        }
      };

  @Test
  void ensureIconBundlerWorks() throws IOException {
    val serviceLoader =
        ImageBundler.resolve(
            CompositeIconDefinition.Format.ICO, ClassLoader.getSystemClassLoader());

    val cfg = mock(SelfExtractingExecutableConfiguration.class);

    val excfg = mock(ExecutableFileConfiguration.class);
    val iconDef = mock(CompositeIconDefinition.class);
    given(cfg.getExecutableFileConfiguration()).willReturn(excfg);
    given(iconDef.getSource())
        .willReturn(new File(ClassLoader.getSystemResource("icons/installer256px.png").getFile()));
    given(excfg.getIconDefinition()).willReturn(iconDef);
    val testDefinition = mock(IconDefinition.class);
    given(testDefinition.getSize()).willReturn(IconDefinition.Size.Size48px);
    List<IconDefinition> iconDefinitions = List.of(testDefinition);
    doReturn(iconDefinitions).when(iconDef).getIconDefinitions();
    val result =
        serviceLoader.bundle(
            cfg, new File(Tests.buildDirectory(), UUID.randomUUID().toString()).toPath(), sysout);

    assertTrue(result.exists());
    val icoFile = ICODecoder.read(result);
    assertEquals(1, icoFile.size());
  }

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
