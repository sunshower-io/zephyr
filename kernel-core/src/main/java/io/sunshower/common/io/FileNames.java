package io.sunshower.common.io;

import lombok.val;

public class FileNames {

  /**
   * @param fileName a filename with extension
   * @return the filename, sans extension
   */
  public static String nameOf(String fileName) {
    if (fileName == null) {
      return null;
    }
    val last = fileName.lastIndexOf('.');
    if (last == -1) {
      return fileName;
    }
    return fileName.substring(0, last);
  }
}
