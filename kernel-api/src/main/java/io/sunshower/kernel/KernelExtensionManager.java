package io.sunshower.kernel;

import java.net.URL;
import java.util.List;

public interface KernelExtensionManager<T extends KernelExtensionDescriptor> {

    List<T> getLoaded();

    boolean unload(T descriptor);

    KernelExtensionLoadTask load(URL url) throws KernelExtensionConflictException, KernelModuleConflictException;

}
