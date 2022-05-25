package io.zephyr.kernel.core.actions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.sunshower.lang.events.EventListener;
import io.zephyr.kernel.core.ModuleManagerTestCase;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.module.ModuleLifecycle.Actions;
import java.util.concurrent.ExecutionException;
import lombok.val;
import org.junit.jupiter.api.Test;

class ModuleDownloadPhaseTest extends ModuleManagerTestCase {

  @Test
  void ensureDownloadingNullDispatchesEvent() throws ExecutionException, InterruptedException {

    val request = new ModuleInstallationRequest();
    request.setLifecycleActions(Actions.Install);
    request.setLocation(null); // explicitly set

    val group = new ModuleInstallationGroup();
    group.add(request);

    val eventListener = mock(EventListener.class);

    kernel.addEventListener(eventListener, ModulePhaseEvents.MODULE_DOWNLOAD_FAILED);

    kernel.getModuleManager().prepare(group).commit().toCompletableFuture().get();
    verify(eventListener, times(1)).onEvent(any(), any());
  }
}
