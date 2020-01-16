package io.zephyr.kernel.modules.shell.console;

import java.rmi.Remote;

public interface Invoker extends Remote {
  void setConsole(Console console) throws Exception;

  Console getConsole() throws Exception;

  /**
   * @param parameters the parameters supplied (may not be null, use Parameters.empty())
   * @return the invocation result
   */
  Result invoke(Parameters parameters) throws Exception;

  /** @return the history for this invoker */
  History getHistory() throws Exception;

  CommandRegistry getRegistry() throws Exception;
}
