package io.zephyr.kernel.service;

import io.zephyr.api.ServiceDefinition;
import io.zephyr.api.ServiceReference;
import io.zephyr.kernel.Module;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;

@ToString
@EqualsAndHashCode
public class DefaultServiceReference<T> implements ServiceReference<T> {

  final Module owner;
  final List<Module> dependentModules;
  final ServiceDefinition<T> definition;

  public DefaultServiceReference(Module owner, ServiceDefinition<T> definition) {
    this.owner = owner;
    this.definition = definition;
    this.dependentModules = new ArrayList<>(0);
  }

  @Override
  public Module getModule() {
    return owner;
  }

  @Override
  public List<Module> getDependentModules() {
    return dependentModules;
  }

  @Override
  public ServiceDefinition<T> getDefinition() {
    return definition;
  }

  @Override
  public boolean isAssignableTo(Module module, String className) {
    try {
      val clazz = Class.forName(className, false, module.getClassLoader());
      return clazz.getClassLoader().equals(module.getClassLoader());
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
