package flywaytest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleContext;
import io.zephyr.kernel.Assembly;
import io.zephyr.kernel.Module;
import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisabledOnOs(OS.WINDOWS)
@ExtendWith(MockitoExtension.class)
class FlywayTestTest {

  static {
    System.setProperty("logging.level.org.flywaydb", "DEBUG");
  }

  @Mock private ModuleContext context;

  @Mock private Module module;

  private File assemblyFile;
  private FlywayTest flywayTest;

  @BeforeEach
  void setUp() {
    flywayTest = new FlywayTest();
    assemblyFile = Tests.relativeToCurrentProjectBuild("war", "libs");
    doReturn(module).when(context).getModule();
    doReturn(new Assembly(assemblyFile)).when(module).getAssembly();
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
      flywayTest.start(context);
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
