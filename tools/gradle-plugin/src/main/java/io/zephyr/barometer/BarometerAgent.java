package io.zephyr.barometer;

import java.lang.instrument.Instrumentation;
import lombok.val;

public class BarometerAgent {
  public static void agentmain(String agentArgs, Instrumentation instrumentation) {
    System.out.println(
        "Starting from " + ProcessHandle.current().pid() + " with args: " + agentArgs);
    val kernel = getInstance(instrumentation);

    System.out.println("Successfully obtained kernel instance: " + kernel);
  }

  static Object getInstance(Instrumentation instrumentation) {

    for (val cls : instrumentation.getAllLoadedClasses()) {
      if (cls.getName().startsWith("io.zephyr")) {

        val classLoader = cls.getClassLoader();
        try {
          val cl = Class.forName("io.zephyr.kernel.core.Framework", true, classLoader);
          val method = cl.getMethod("getInstance");
          return method.invoke(null);
        } catch (Exception e) {

        }
      }
    }
    System.out.println("failed to obtain kernel instance (is it running?)");
    return null;
  }
}
