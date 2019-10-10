package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.io.ObservableChannelTransferListener;
import java.io.File;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public abstract class AbstractKernelExtensionLoadTask<
        T extends KernelExtensionDescriptor, U extends KernelExtensionLoadTask<T, U>>
    extends ObservableChannelTransferListener implements KernelExtensionLoadTask<T, U> {
  private State state;

  private volatile boolean paused;

  private final URL source;
  private final File destination;
  private final CompletableFuture<U> future;
  private final Localization localization;
  private final ExecutorService executorService;
  private final MonitorableFileTransfer callable;

  protected final KernelExtensionManager<?, ?, T, U> manager;

  @Getter private Throwable error;

  public AbstractKernelExtensionLoadTask(
      @NonNull KernelExtensionManager<?, ?, T, U> manager,
      @NonNull URL source,
      @NonNull File target,
      @NonNull MonitorableFileTransfer callable,
      @NonNull ExecutorService service) {
    this.callable = callable;
    this.manager = manager;
    this.source = source;
    this.destination = target;
    this.executorService = service;
    this.future = new CompletableFuture<>();
    this.localization = manager.getLocalization();
    state = State.Unstarted;
  }

  int count = 0;

  @Override
  public URL getSource() {
    return source;
  }

  @Override
  public File getLoadedFile() {
    return destination;
  }

  @Override
  public File getExtensionDirectory() {
    return destination.getAbsoluteFile().getParentFile();
  }

  @Override
  public void start() {
    state = State.Running;
    executorService.submit(callable);
  }

  @Override
  public void onTransfer(ReadableByteChannel channel, double progress) {
    if (count++ % 100 == 0) {
      System.out.println(progress);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onComplete(ReadableByteChannel channel) {
    log.info(localization.format("plugin.download.complete", source, destination));
    future.complete((U) this);
    this.state = State.Completed;
    super.onComplete(channel);
    manager.getInflight().remove(this);
  }

  @Override
  public void onError(ReadableByteChannel channel, Exception ex) {
    log.info(localization.format("plugin.download.error", source, destination));
    future.obtrudeException(ex);
    this.error = ex;
    this.state = State.Error;
    super.onError(channel, ex);
  }

  @Override
  public void onCancel(ReadableByteChannel channel) {
    log.info(localization.format("plugin.download.cancel", source, destination));
    future.cancel(true);
    this.state = State.Cancelled;
    super.onCancel(channel);
  }

  @Override
  public boolean isComplete() {
    return state == State.Completed;
  }

  @Override
  public synchronized void pause() {
    throw new UnsupportedOperationException("Cannot currently pause plugin download");
  }

  @Override
  public synchronized void resume() {
    throw new UnsupportedOperationException("Cannot currently resume plugin download");
  }

  @Override
  public U restart() throws KernelExtensionConflictException {
    cancel();
    removeListener(this);
    return manager.loadExtensionFile(source);
  }

  @Override
  public void cancel() {
    log.info(localization.format("plugin.cancelling", source));
    Thread.currentThread().interrupt();
    if (!destination.delete()) {
      log.warn(localization.format("cancelled.plugin.temp.deletion.failed", source, destination));
    }
    log.info(localization.format("plugin.cancelled", source));
    state = State.Cancelled;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public CompletableFuture<T> getFuture() {
    if (state == State.Unstarted) {
      throw new IllegalStateException(localization.format("plugin.load.unstarted", source));
    }
    return future.thenApplyAsync(this::doLoad);
  }

  protected T doLoad(U task) {
    val result = extract(task);
    manager.registerDescriptor(result);
    return result;
  }

  protected abstract T extract(U task);
}
