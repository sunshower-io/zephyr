package io.zephyr.kernel.core.actions;

import io.zephyr.kernel.events.EventType;

public enum ModulePhaseEvents implements EventType {

  /** events dispatched for download status/tracking */
  MODULE_DOWNLOAD_INITIATED,
  MODULE_DOWNLOAD_COMPLETED,
  MODULE_DOWNLOAD_FAILED,

  /** */
  MODULE_SCAN_INITIATED,
  MODULE_SCAN_COMPLETED,
  MODULE_SCAN_FAILED,

  /** */
  MODULE_TRANSFER_INITIATED,
  MODULE_TRANSFER_COMPLETED,
  MODULE_TRANSFER_FAILED,

  /** */
  MODULE_ASSEMBLY_EXTRACTION_INITIATED,
  MODULE_ASSEMBLY_EXTRACTION_COMPLETED,
  MODULE_ASSEMBLY_EXTRACTION_FAILED,

  /** */
  MODULE_FILESYSTEM_CREATION_INITIATED,
  MODULE_FILESYSTEM_CREATION_COMPLETED,
  MODULE_FILESYSTEM_CREATION_FAILED,

  /** lifecycle for module collection events. These are generally interal */
  MODULE_SET_INSTALLATION_COMPLETED,
  MODULE_SET_INSTALLATION_INITIATED,
  ;

  private final int value;

  ModulePhaseEvents() {
    value = EventType.newId();
  }

  @Override
  public int getId() {
    return value;
  }
}
