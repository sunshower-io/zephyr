package io.zephyr.kernel.launch;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class KernelLauncherTest {

  @Test
  void ensureRegistryIsCreated() {
    val ctx = KernelLauncher.doLaunch(new String[0]);
    val registry = (EntryPointRegistry) ctx.get(EntryPoint.ContextEntries.ENTRY_POINT_REGISTRY);
    assertNotNull(registry, "registry must not be null");
  }

  @Test
  void ensureMockEntryPointInitializeIsCalledExactlyOnce() {
    val mep = runAndGetMock();
    val ctx = mep.getContext();
    verify(mep.getMock(), times(1)).initialize(eq(ctx));
  }

  @Test
  void ensureMockEntryPointStartIsCalledExactlyOnce() {
    val mep = runAndGetMock();
    verify(mep.getMock(), times(1)).start();
  }

  @Test
  void ensureMockEntryPointRunIsCalledExactlyOnce() {
    val mep = runAndGetMock();
    verify(mep.getMock(), times(1)).run(mep.getContext());
  }

  @Test
  void ensureStopIsCalledExactlyOnce() {
    val mep = runAndGetMock();
    verify(mep.getMock(), times(1)).stop();
  }

  @Test
  void ensureFinalizeIsCalledExactlyOnce() {
    val mep = runAndGetMock();
    verify(mep.getMock(), times(1)).finalize(mep.getContext());
  }

  @Test
  void ensureRegistryContainsCorrectEntryPointCount() {
    val ctx = runAndGetMock().getContext();
    val registry = (EntryPointRegistry) ctx.get(EntryPoint.ContextEntries.ENTRY_POINT_REGISTRY);
    assertEquals(2, registry.getEntryPoints().size() , "must contain 2 entrypoints");
  }

  MockEntryPoint runAndGetMock() {
    return runAndGetMock(new String[0]);
  }

  MockEntryPoint runAndGetMock(String[] args) {
    val ctx = KernelLauncher.doLaunch(args);
    val registry = (EntryPointRegistry) ctx.get(EntryPoint.ContextEntries.ENTRY_POINT_REGISTRY);
    return (MockEntryPoint)
        registry.getEntryPoints(t -> t.getClass().equals(MockEntryPoint.class)).get(0);
  }
}
