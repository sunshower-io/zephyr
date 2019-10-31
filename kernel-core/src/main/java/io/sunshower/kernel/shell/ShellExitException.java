package io.sunshower.kernel.shell;

/** Throw this when a command must exit--otherwise everything will be reprocessed */
public class ShellExitException extends RuntimeException {

  static final int serialVersionUID = 241234;
}
