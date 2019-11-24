package io.sunshower.kernel.core;

import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;

@ZephyrTest
@Modules(@Module(project = "kernel-modules:sunshower-yaml-reader", type = Module.Type.KernelModule))
class KernelRegistryMementoSystemTest {}
