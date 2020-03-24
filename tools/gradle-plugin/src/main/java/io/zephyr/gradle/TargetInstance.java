package io.zephyr.gradle;

import com.sun.tools.attach.VirtualMachine;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.SneakyThrows;
import lombok.val;

public class TargetInstance {
  static final Logger log = Logger.getLogger(TargetInstance.class.getName());

  private final VirtualMachine virtualMachine;

  public TargetInstance(VirtualMachine virtualMachine) {
    this.virtualMachine = virtualMachine;
  }

  @SneakyThrows
  public void connect() {
    log.log(Level.INFO, "Connecting to " + virtualMachine.getAgentProperties());
    val file = TargetInstance.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    log.log(Level.INFO, "Agent source: {0}", file);
    virtualMachine.loadAgent(file, "frapper");
  }
}
