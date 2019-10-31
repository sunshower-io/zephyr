package io.sunshower.kernel.state.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.io.IOException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
class CoordinateOriginatorTest {

  private File directory;
  private File stateFile;

  @BeforeEach
  void beforeEach() throws IOException {
    directory = Tests.createTemp("memento-state");
    stateFile = new File(directory, "kernel-state.ssk");
    if (!(stateFile.exists() || stateFile.createNewFile())) {
      throw new IllegalStateException("Couldn't create state file");
    }
  }

  @Test
  void ensureSavingCoordinateWorks() {
    val mementoContext = new RecursiveMementoContext(stateFile);
    var coordinate = ModuleCoordinate.create("test-group", "test-name", "1.0.0-SNAPSHOT");
    mementoContext.save(coordinate);
    coordinate = mementoContext.restore(Coordinate.class);
    assertEquals(coordinate.getName(), "test-name", "name must be restored correctly");
  }
}
