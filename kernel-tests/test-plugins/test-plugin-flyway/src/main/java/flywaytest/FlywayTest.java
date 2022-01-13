package flywaytest;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.support.flyway.FlywaySupport;
import org.flywaydb.core.Flyway;

public class FlywayTest implements ModuleActivator {

  static final String CONNECTION_STRING =
      "jdbc:h2:mem:testdb;USER=sa;PASSWORD=password;DB_CLOSE_DELAY=-1";

  @Override
  public void start(ModuleContext context) {
    context.getModule();
    System.out.println("Plugin1 starting...");
    try {
      Class.forName("org.h2.Driver", true, Thread.currentThread().getContextClassLoader());
      System.out.println("Creating connection...");
      Flyway.configure()
          .table("schema_version")
          .dataSource(CONNECTION_STRING, "sa", "password")
          .ignoreMissingMigrations(true)
          .cleanDisabled(true)
          .outOfOrder(false)
          .resourceProvider(FlywaySupport.classpath(context.getModule()).locations("flyway"))
          .load()
          .migrate();
    } catch (Exception ex) {
      System.out.println("Failed to load org.h2.Driver");
      ex.printStackTrace();
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("Plugin1 stopping...");
  }
}
