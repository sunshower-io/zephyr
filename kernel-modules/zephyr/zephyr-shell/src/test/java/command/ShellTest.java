package command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.modules.shell.command.DaggerShellInjectionConfiguration;
import io.zephyr.kernel.modules.shell.command.DefaultCommandContext;
import io.zephyr.kernel.modules.shell.command.Shell;
import io.zephyr.kernel.modules.shell.console.Command;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Parameters;
import io.zephyr.kernel.modules.shell.console.Result;
import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import picocli.CommandLine;

@DisabledOnOs(OS.MAC)
@SuppressFBWarnings
@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ShellTest {

  private Shell shell;
  private CommandContext context;

  @BeforeEach
  void setUp() {
    context = spy(new DefaultCommandContext(Collections.emptyMap()));
    shell = doCreate();
  }

  @Test
  void ensureInvokingCommandWorks() throws Exception {
    Command c = createCommand();

    shell.getRegistry().register(c);
    shell.invoke(Parameters.of("frapper"));
    verify(c, times(1)).execute(any(CommandContext.class));
  }

  @Test
  void ensureInvokedCommandAppearsInCommandHistory() throws RemoteException {
    val c = createCommand();

    shell.getRegistry().register(c);
    shell.invoke(Parameters.of("frapper"));
    assertTrue(shell.getHistory().getHistory().contains(c), "must contain command in history");
  }

  private Shell doCreate() {
    return doCreate(System.in, System.out);
  }

  private Shell doCreate(InputStream inputStream, PrintStream outputStream) {
    val cfg =
        DaggerShellInjectionConfiguration.factory()
            .create(ClassLoader.getSystemClassLoader(), context, inputStream, outputStream);
    return cfg.createShell();
  }

  private Command createCommand() {
    @CommandLine.Command(name = "frapper")
    class C implements Command {
      private static final long serialVersionUID = -5711260216340918001L;

      @Override
      public String getName() {
        return "frapper";
      }

      @Override
      public Result execute(CommandContext context) {
        return null;
      }
    }
    return spy(new C());
  }
}
