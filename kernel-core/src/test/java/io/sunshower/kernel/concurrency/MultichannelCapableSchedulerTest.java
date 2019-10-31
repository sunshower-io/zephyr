package io.sunshower.kernel.concurrency;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.EmptyCatchBlock",
  "PMD.JUnitUseExpected",
  "PMD.AvoidDuplicateLiterals",
  "PMD.UseProperClassLoader",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage",
})
class MultichannelCapableSchedulerTest {

  private ScheduledExecutorService service;
  private MultichannelCapableScheduler scheduler;

  @BeforeEach
  void setUp() {
    service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    scheduler = new MultichannelCapableScheduler(service);
  }

  @Test
  void ensureSubmittingTaskWorks() {
    val processor = mock(Processor.class);
    given(processor.getChannel()).willReturn("test");
    doAnswer(
            invocation -> {
              ConcurrentProcess proc = invocation.getArgument(0);
              proc.perform();
              return null;
            })
        .when(processor)
        .process(any());
    scheduler.registerHandler(processor);
    scheduler.start("test");
    val task = spy(task("test"));
    scheduler.scheduleTask(task);
    scheduler.awaitShutdown();
    verify(task, times(1)).perform();
  }

  ConcurrentProcess task(String channel) {
    return new TestProcess(channel);
  }

  @AllArgsConstructor
  static class TestProcess implements ConcurrentProcess {
    final String channel;

    @Override
    public String getChannel() {
      return channel;
    }

    @Override
    public void perform() {}
  }
}
