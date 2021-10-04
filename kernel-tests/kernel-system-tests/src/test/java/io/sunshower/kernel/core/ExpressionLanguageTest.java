package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.kernel.test.Clean;
import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.Context;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.Query;
import io.zephyr.cli.Zephyr;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@ZephyrTest
@Modules({
  @Module(project = "kernel-modules:sunshower-yaml-reader", type = Module.Type.KernelModule)
})
@DisabledOnOs(OS.WINDOWS)
@Clean(value = Clean.Mode.After, context = Clean.Context.Method)
public class ExpressionLanguageTest {

  @Inject private Zephyr zephyr;
  @Inject private ModuleContext context;

  public static class Ctx {
    public String name = "whatever";
  }

  @Test
  void ensureMVELIsResolvable() {
    zephyr.install(StandardModules.MVEL.getUrl());
    zephyr.restart();

    val query = new Query<>("value.name == 'whatever'", "mvel", new Context<>(null, null));
    val predicate = context.createFilter(query);
    assertTrue(predicate.test(new Ctx()), "name must be whatever");
  }
}
