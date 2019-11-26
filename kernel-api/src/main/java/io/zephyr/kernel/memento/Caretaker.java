package io.zephyr.kernel.memento;

import java.util.concurrent.CompletionStage;

public interface Caretaker {

  CompletionStage<Void> persistState() throws Exception;

  CompletionStage<Void> restoreState() throws Exception;
}
