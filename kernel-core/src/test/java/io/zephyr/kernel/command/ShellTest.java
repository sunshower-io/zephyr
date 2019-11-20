package io.zephyr.kernel.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.zephyr.api.Command;
import io.zephyr.api.CommandContext;
import io.zephyr.api.Parameters;
import io.zephyr.api.Result;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.*;
import java.rmi.RemoteException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

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
    context = spy(new DefaultCommandContext());
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
