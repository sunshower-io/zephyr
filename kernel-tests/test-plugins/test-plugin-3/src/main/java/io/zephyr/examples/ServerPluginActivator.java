package io.zephyr.examples;

import io.zephyr.*;

public class ServerPluginActivator implements PluginActivator {

  private RequirementRegistration<TranslationService> serverRequirement;

  @Override
  public void initialize(PluginContext ctx) throws Exception {
    //    serverRequirement = ctx.createRequirement(Requirements.create(TranslationService.class));
  }

  @Override
  public void start(PluginContext context) throws Exception {}

  @Override
  public void stop(PluginContext context) throws Exception {
    //    serverRequirement.close();
  }
}
