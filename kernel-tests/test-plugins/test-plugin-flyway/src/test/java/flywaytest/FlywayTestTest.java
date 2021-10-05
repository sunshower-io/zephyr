package flywaytest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.zephyr.api.ModuleContext;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlywayTestTest {

  static {
    System.setProperty("logging.level.org.flywaydb", "DEBUG");
  }

  private FlywayTest flywayTest;

  @BeforeEach
  void setUp() {
    flywayTest = new FlywayTest();
  }

  @AfterEach
  void tearDown() {}

  @Test
  @SneakyThrows
  void ensureStartingModuleWorksAndPerformsMigration() {
    try (var connection = DriverManager.getConnection(FlywayTest.CONNECTION_STRING)) {
      assertThrows(
          SQLException.class,
          () -> {
            connection.createStatement().executeQuery("select * from MYTESTTABLE");
          });
      flywayTest.start(mock(ModuleContext.class));
      //      connection.createStatement().executeQuery("select * from MyTestTable");
      System.out.println("Tables:");
      val tables = connection.createStatement().executeQuery("show tables");
      while (tables.next()) {
        val name = tables.getString("table_name");
        System.out.println(name);
      }
      // should not throw an exception
      connection.createStatement().executeQuery("select * from MYTESTTABLE");
    } finally {
      flywayTest.stop(mock(ModuleContext.class));
    }
  }
}
