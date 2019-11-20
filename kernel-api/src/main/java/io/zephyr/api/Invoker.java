package io.zephyr.api;

import java.rmi.Remote;

public interface Invoker extends Remote {

  /** @throws Exception */
  void start() throws Exception;

  /**
   * since the main thread is blocked by start(), this will need to be invoked by the invoker
   * (separate thread)
   *
   * @throws Exception
   */
  void stop() throws Exception;

  /**
   * @param parameters the parameters supplied (may not be null, use Parameters.empty())
   * @return the invocation result
   */
  Result invoke(Parameters parameters) throws Exception;

  /** @return the history for this invoker */
  History getHistory() throws Exception;

  CommandRegistry getRegistry() throws Exception;
}
