package io.zephyr.kernel.command;

import io.zephyr.api.Command;
import io.zephyr.api.CommandContext;
import io.zephyr.api.Parameters;
import io.zephyr.kernel.command.kernel.StartKernelCommand;
import io.zephyr.kernel.core.Kernel;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class LocalShellTest {

  private Shell shell;
  private Kernel kernel;
  private CommandContext context;

  @BeforeEach
  void setUp() {
    kernel = mock(Kernel.class);
    context = spy(new DefaultCommandContext(kernel));
    shell = doCreate();
  }

  @Test
  void ensureInvokingCommandWorks() {
    val c = mock(Command.class);

    given(c.getName()).willReturn("frapper");
    shell.getRegistry().register(c);
    shell.invoke("frapper", Parameters.empty());
    verify(c, times(1)).invoke(any(CommandContext.class), eq(Parameters.empty()));
  }

  @Test
  void ensureInvokedCommandAppearsInCommandHistory() {
    val c = mock(Command.class);

    given(c.getName()).willReturn("frapper");
    shell.getRegistry().register(c);
    shell.invoke("frapper", Parameters.empty());
    assertTrue(shell.getHistory().getHistory().contains(c), "must contain command in history");
  }

  @Test
  void ensureStartKernelCommandWorks() {
    val a = new StartKernelCommand();
    shell.getRegistry().register(a);
    shell.invoke(StartKernelCommand.name, Parameters.empty());
    verify(context, times(1)).getKernel();
    verify(kernel, times(1)).start();
  }

  @Test
  void ensureStartingKernelWorks() throws Exception {

    val is = new ByteArrayInputStream("kernel start\n".getBytes());
    val os = new PrintStream(new ByteArrayOutputStream());
    val shell = doCreate(is, os);

    shell.start();
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
}
