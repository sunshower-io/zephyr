package io.zephyr.kernel.launch;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import java.util.Map;
import java.util.logging.Logger;
import lombok.Getter;

public class MockEntryPoint implements EntryPoint {

  static final Logger log = Logger.getLogger(MockEntryPoint.class.getName());

  @Getter private final EntryPoint mock;
  @Getter private Map<ContextEntries, Object> context;

  public MockEntryPoint() {
    mock = mock(EntryPoint.class);
    given(mock.getLogger()).willReturn(log);
  }

  @Override
  public Logger getLogger() {
    return mock.getLogger();
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    this.context = context;
    mock.initialize(context);
  }

  @Override
  @SuppressWarnings("PMD.FinalizeOverloaded")
  public void finalize(Map<ContextEntries, Object> context) {
    mock.finalize(context);
  }

  @Override
  public void start() {
    mock.start();
  }

  @Override
  public void stop() {
    mock.stop();
  }

  @Override
  public <T> T getService(Class<T> type) {
    return null;
  }

  @Override
  public <T> boolean exports(Class<T> type) {
    return false;
  }

  @Override
  public void run(Map<ContextEntries, Object> ctx) {
    mock.run(ctx);
  }

  @Override
  public Options<?> getOptions() {
    return null;
  }

  @Override
  public int getPriority() {
    return 0;
  }
}
