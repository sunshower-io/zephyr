package io.sunshower.kernel.concurrency;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface TaskTracker<E>
    extends CompletionStage<Context>, TaskEventObservable<E>, Future<Context> {}
