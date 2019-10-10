package io.sunshower.kernel;

import io.sunshower.kernel.common.i18n.Localization;

import java.net.URL;
import java.util.List;

public interface KernelExtensionManager<T extends KernelExtensionDescriptor, U extends KernelExtensionLoadTask<T, U>> {

    boolean register(T descriptor) throws KernelExtensionConflictException;

    List<U> getInflight();

    Localization getLocalization();

    List<T> getLoaded();

    boolean unload(T descriptor);

    U load(URL url) throws KernelExtensionConflictException;

}
