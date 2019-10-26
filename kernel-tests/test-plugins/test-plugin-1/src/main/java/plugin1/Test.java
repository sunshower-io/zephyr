package plugin1;

import io.sunshower.kernel.core.ModuleActivator;
import io.sunshower.kernel.core.ModuleContext;
import lombok.val;

public class Test implements ModuleActivator {
  public Test() {
    val text = getClass().getResource("/test.txt");
    if (text == null) {
      throw new IllegalStateException();
    }
  }

  @Override
  public void onLifecycleChanged(ModuleContext context) {
    System.out.println("LIFECYCLE CHANGED HOMESLICE!");
  }
}
