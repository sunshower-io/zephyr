package plugin1;

public class Plugin1Service implements Service {
  @Override
  public String getHello() {
    return "Hello from 1.0";
  }

  @Override
  public void sayHello() {
    System.out.println("Hello from service1.0");
  }
}
