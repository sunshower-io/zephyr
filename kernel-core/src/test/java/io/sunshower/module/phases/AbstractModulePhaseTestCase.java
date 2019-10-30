package io.sunshower.module.phases;

import io.sunshower.kernel.core.DaggerSunshowerKernelConfiguration;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.core.SunshowerKernelInjectionModule;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractModulePhaseTestCase {

  protected File sunshowerHome;
  protected KernelOptions options;
  protected SunshowerKernel kernel;
  protected KernelProcessContext context;
  protected FileSystem kernelFileSystem;
  protected DependencyGraph dependencyGraph;

  @BeforeEach
  void setUp() throws Exception {
    sunshowerHome = Tests.createTemp(".sunshower-home");
    options = new KernelOptions();
    options.setHomeDirectory(sunshowerHome);
    SunshowerKernel.setKernelOptions(options);

    val injectionModule =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(options, ClassLoader.getSystemClassLoader()))
            .build();

    kernelFileSystem =
        FileSystems.newFileSystem(URI.create("droplet://deploy"), Collections.emptyMap());
    kernel = (SunshowerKernel) injectionModule.kernel();
    kernel.setFileSystem(kernelFileSystem);
    dependencyGraph = injectionModule.dependencyGraph();
    context = new KernelProcessContext(kernel);
  }

  @AfterEach
  void tearDown() throws IOException {
    kernelFileSystem.close();
  }
}
