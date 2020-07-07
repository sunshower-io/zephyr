package io.zephyr.bundle.sfx.formats;

import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.icons.IconDefinition;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

public class ICO {

  public static List<BufferedImage> resize(BufferedImage file, List<IconDefinition.Size> all) {
    return resize(file, all, Log.NOOP);
  }

  public static List<BufferedImage> resize(
      BufferedImage file, List<IconDefinition.Size> all, Log log) {
    List<BufferedImage> images = new ArrayList<>();
    for (val size : all) {
      log.info("Generating icon(w:%s,h:%s)...", size.getWidth(), size.getHeight());
      images.add(resize(file, size));
      log.info("Successfully generated icon(w:%s,h:%s)", size.getWidth(), size.getHeight());
    }
    return images;
  }

  public static BufferedImage resize(BufferedImage file, IconDefinition.Size size) {
    val temp = file.getScaledInstance(size.getWidth(), size.getHeight(), Image.SCALE_SMOOTH);
    val resized = new BufferedImage(size.getWidth(), size.getHeight(), BufferedImage.TYPE_INT_ARGB);
    val graphics = resized.createGraphics();
    graphics.drawImage(temp, 0, 0, null);
    graphics.dispose();
    return resized;
  }
}
