package io.zephyr.barometer;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.kernel.core.Kernel;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class BarometerModule implements ModuleActivator, ProcessIdListener {
  static final Logger log = Logger.getLogger(BarometerModule.class.getName());

  static final String zephyrPidFile = "zephyr.pid";
  static final String zephyrAgentFile = "agent.pid";

  private ProcessIdProtocol protocol;

  @Override
  public void start(ModuleContext context) {
    val kernel = context.unwrap(Kernel.class);
    val dataDir = kernel.getFileSystem().getPath("barometer").toFile();
    if (!(dataDir.exists() || dataDir.mkdirs())) {
      log.log(
          Level.SEVERE,
          "Failed to create or obtain Barometer data directory {0}",
          dataDir.getAbsolutePath());
    }

    protocol =
        new ProcessIdProtocol(
            dataDir,
            zephyrPidFile,
            zephyrAgentFile,
            this,
            kernel.getScheduler().getKernelExecutor());
    protocol.execute();
  }

  @Override
  public void stop(ModuleContext context) {
    protocol.close();
  }

  @Override
  public void onProcessIdDiscovered(long pid) {
    log.log(Level.SEVERE, "Obtained PID: {0}", pid);
  }
}
