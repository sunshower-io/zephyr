package io.zephyr.examples;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.RequirementRegistration;

public class ServerPluginActivator implements ModuleActivator {

  private RequirementRegistration<TranslationService> serverRequirement;

  @Override
  public void initialize(ModuleContext ctx) throws Exception {
    //    serverRequirement = ctx.createRequirement(Requirements.create(TranslationService.class));
  }

  @Override
  public void start(ModuleContext context) throws Exception {}

  @Override
  public void stop(ModuleContext context) throws Exception {
    //    serverRequirement.close();
  }
}
