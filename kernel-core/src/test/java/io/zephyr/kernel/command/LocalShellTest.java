package io.zephyr.kernel.command;

import io.zephyr.api.Command;
import io.zephyr.api.CommandContext;
import io.zephyr.api.Parameters;
import io.zephyr.kernel.core.Kernel;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.rmi.RemoteException;

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
    context = spy(new DefaultCommandContext());
    shell = doCreate();
  }

  @Test
  void ensureInvokingCommandWorks() throws Exception {
    val c = mock(Command.class);

    given(c.getName()).willReturn("frapper");
    shell.getRegistry().register(c);
    shell.invoke(Parameters.of("frapper"));
    verify(c, times(1)).execute(any(CommandContext.class));
  }

  @Test
  void ensureInvokedCommandAppearsInCommandHistory() throws RemoteException {
    val c = mock(Command.class);

    given(c.getName()).willReturn("frapper");
    shell.getRegistry().register(c);
    shell.invoke(Parameters.of("frapper"));
    assertTrue(shell.getHistory().getHistory().contains(c), "must contain command in history");
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
