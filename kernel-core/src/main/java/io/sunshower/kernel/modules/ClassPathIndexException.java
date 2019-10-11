package io.sunshower.kernel.modules;

public class ClassPathIndexException extends RuntimeException {
  public ClassPathIndexException(Throwable e) {
    super(e);
  }

  public ClassPathIndexException(String s) {
    super(s);
  }
}
