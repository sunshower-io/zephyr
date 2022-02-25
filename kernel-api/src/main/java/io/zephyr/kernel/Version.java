package io.zephyr.kernel;

public interface Version extends Comparable<Version> {

  boolean satisfies(String range);

}
