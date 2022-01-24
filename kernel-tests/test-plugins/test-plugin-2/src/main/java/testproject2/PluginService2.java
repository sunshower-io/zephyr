package testproject2;

import plugin1.Service;

public class PluginService2 implements Service {

  @Override
  public void sayHello() {
    System.out.println("Hello from service 2");
  }
}
