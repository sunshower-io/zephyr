package io.zephyr.kernel;

import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

public interface TaskQueue extends Startable, Stoppable {

  int getOutstandingTasks();

  <T> CompletionStage<T> schedule(Callable<T> task);

  CompletionStage<Void> schedule(Runnable task);
}
