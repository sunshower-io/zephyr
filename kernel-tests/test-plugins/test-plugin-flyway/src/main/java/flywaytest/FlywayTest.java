package flywaytest;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

public class FlywayTest implements ModuleActivator {

  static final String CONNECTION_STRING = "jdbc:h2:mem:2";

  @Override
  public void start(ModuleContext context) {
    System.out.println("Plugin1 starting...");
    try {
      Class.forName("org.h2.Driver", true, Thread.currentThread().getContextClassLoader());
      System.out.println("Creating connection...");
      Flyway.configure()
          .table("schema_version")
          .dataSource(CONNECTION_STRING, "", "")
          .ignoreMissingMigrations(true)
          .cleanDisabled(true)
          .outOfOrder(true)
          .resourceProvider(
              new ResourceProvider() {
                @Override
                public LoadableResource getResource(String name) {
                  return null;
                }

                @Override
                public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
                  return List.of(
                      new UrlResourceLoadableResource(
                          classLoaderUrl("flyway/V1_1__sample-test.sql")));
                }

                private URL classLoaderUrl(String s) {
                  return Thread.currentThread().getContextClassLoader().getResource(s);
                }
              })
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
