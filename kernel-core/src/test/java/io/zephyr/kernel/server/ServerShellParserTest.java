package io.zephyr.kernel.server;

import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.shell.ShellParser;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static org.mockito.Mockito.mock;

class ServerShellParserTest {
  int port;
  ShellParser parser;
  KernelOptions options;

  @BeforeEach
  void setUp() throws RemoteException, AlreadyBoundException {
    port = 9999;
    options = new KernelOptions();
    options.setPort(port);
  }

  @Test
  void ensureShellParserStartsCorrectly() throws Exception {
    val registry = LocateRegistry.createRegistry(port);
    val parser = new ServerShellParser(options, mock(ShellParser.class));
    try {
      parser.start();
      val remote = (ShellParser) registry.lookup(ServerShellParser.NAME);
      remote.perform(options, new String[] {"hello", "world"});

      //      result.perform(options, new String[] {"hello", "world"});
    } finally {
      if (parser.isRunning()) {
        parser.stop();
      }
    }
  }
}
