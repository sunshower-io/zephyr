package testproject2;

import lombok.val;

public class Test {
  public Test() {
    val plugin = new plugin1.Test();
    System.out.println(plugin);
  }
}
