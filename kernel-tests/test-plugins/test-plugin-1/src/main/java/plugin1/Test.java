package plugin1;

import lombok.val;

public class Test {
  public Test() {
    val text = getClass().getResource("/test.txt");
    if (text == null) {
      throw new IllegalStateException();
    }
  }
}
