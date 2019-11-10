package io.sunshower.kernel.concurrency;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface TaskTracker<E>
    extends CompletionStage<Process<E>>, TaskEventObservable<E>, Future<Process<E>> {}
