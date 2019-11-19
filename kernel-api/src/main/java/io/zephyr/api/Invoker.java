package io.zephyr.api;

public interface Invoker {

  /**
   * this should generally block the main thread
   *
   * @throws Exception
   */
  void start() throws Exception;

  /**
   * since the main thread is blocked by start(), this will need to be invoked by the invoker
   * (separate thread)
   *
   * @throws Exception
   */
  void stop() throws Exception;

  /**
   * @param commandName the name of the command to invoke
   * @param parameters the parameters supplied (may not be null, use Parameters.empty())
   * @return the invocation result
   */
  Result invoke(String commandName, Parameters parameters);

  /** @return the history for this invoker */
  History getHistory();

  CommandRegistry getRegistry();
}
