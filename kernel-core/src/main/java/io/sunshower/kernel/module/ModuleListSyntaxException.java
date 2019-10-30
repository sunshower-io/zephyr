package io.sunshower.kernel.module;

import io.sunshower.kernel.core.KernelException;
import java.io.IOException;

public class ModuleListSyntaxException extends KernelException {
  static final int serialVersionUID = 134132415;

  public ModuleListSyntaxException() {}

  public ModuleListSyntaxException(IOException e) {
    super(e);
  }
}
