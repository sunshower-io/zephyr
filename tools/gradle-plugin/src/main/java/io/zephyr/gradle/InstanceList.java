package io.zephyr.gradle;

import static io.zephyr.common.io.Files.doCheck;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import lombok.val;

@AllArgsConstructor
public class InstanceList {
  static final Logger logger = Logger.getLogger(InstanceList.class.getName());

  final File rootDir;

  @Synchronized
  public void save(Instance instance) throws IOException {
    val result = new File(rootDir, "instances.list");
    val resultPath = result.toPath().toAbsolutePath();
    doCheck(resultPath);
    try (val output =
        new PrintWriter(
            Files.newOutputStream(
                result.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.DSYNC))) {
      output.write(instance.rootDir);
      output.write(File.separator);
    }
  }

  @Synchronized
  public Set<Instance> load() throws IOException {
    val result = new File(rootDir, "instances.list");
    val resultPath = result.toPath().toAbsolutePath();
    doCheck(resultPath);
    val lines = Files.readAllLines(resultPath);
    return lines.stream().map(Instance::new).collect(Collectors.toSet());
  }

  @Synchronized
  public TargetInstance connect() throws IOException {
    val instances = load();
    for (val instance : instances) {
      try {
        return connect(instance);
      } catch (Exception ex) {
        logger.log(
            Level.WARNING,
            "Unable to connect to instance at {0}.  Reason: {1} ",
            new Object[] {instance.rootDir, ex.getMessage()});
      }
    }
    throw new NoSuchElementException("No running instances found");
  }

  @Synchronized
  public TargetInstance connect(Instance instance) throws IOException, AttachNotSupportedException {
    val dir = new File(rootDir, instance.rootDir).getAbsoluteFile();
    val dirPath = dir.toPath();
    doCheck(dirPath);
    val pidf = dirPath.resolve(Paths.get("kernel", "barometer", "zephyr.pid"));
    val pid = Long.parseLong(Files.readString(pidf));
    return new TargetInstance(VirtualMachine.attach(String.valueOf(pid)));
  }

  @AllArgsConstructor
  public static class Instance {
    final String rootDir;
  }
}
