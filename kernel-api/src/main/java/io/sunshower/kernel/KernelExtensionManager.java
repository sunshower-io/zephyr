package io.sunshower.kernel;

import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.events.KernelExtensionEvent;
import java.net.URL;
import java.util.List;

public interface KernelExtensionManager<
        L extends KernelEventListener,
        E extends KernelExtensionEvent,
        T extends KernelExtensionDescriptor,
        U extends KernelExtensionLoadTask<T, U>>
    extends EventDispatcher<L, E> {

  List<U> getInflight();

  Localization getLocalization();

  List<T> getLoaded();

  boolean unload(T descriptor);

  boolean registerExtension(T descriptor);

  boolean registerDescriptor(T descriptor);

  U loadExtensionFile(URL url) throws KernelExtensionException;
}
