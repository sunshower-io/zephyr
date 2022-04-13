package plugin2;

import io.zephyr.api.ServiceReference;

public class Plugin2Service implements Service {

  public Plugin2Service(Object target){
    System.out.println("GOT: " + target.getClass());
    System.out.println("FROM: " + target.getClass().getClassLoader());

  }

  @Override
  public void sayHello() {
    System.out.println("Hello from service2");
  }
}
