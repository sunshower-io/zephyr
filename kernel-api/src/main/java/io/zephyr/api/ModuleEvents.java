package io.zephyr.api;

import io.zephyr.kernel.events.EventType;
import lombok.Getter;

/** */
public enum ModuleEvents implements EventType {

  /** lifecycle for plugin collection events. These are generally interal */
  PLUGIN_SET_INSTALLATION_INITIATED,
  PLUGIN_SET_INSTALLATION_COMPLETE,

  /** Dispatched when a plugin install process begins */
  INSTALLING,

  /** Dispatched when a plugin has been installed correctly */
  INSTALLED,

  /** Dispatched when a plugin has failed to install correctly */
  INSTALL_FAILED,

  /** Dispatched when a plugin begins to resolve */
  RESOLVING,

  /** Dispatched when a plugin has failed to resolve */
  RESOLUTION_FAILED,

  /** Dispatched when a plugin has resolved correctly */
  RESOLVED,

  /** Dispatched when a plugin start process is initiated */
  STARTING,

  /** Dispatched when a plugin has failed to start */
  START_FAILED,

  /** Dispatched when a plugin has successfully started */
  STARTED,

  /** Dispatched when a plugin has begun to stop */
  STOPPING,

  /** Dispatched when a plugin has failed to stop */
  STOP_FAILED,

  /** Dispatched when a plugin has successfully stopped */
  STOPPED;

  @Getter private final int id;

  ModuleEvents() {
    id = EventType.newId();
  }
}
