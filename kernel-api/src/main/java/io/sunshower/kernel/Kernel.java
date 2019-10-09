package io.sunshower.kernel;

import java.net.URL;
import java.util.concurrent.Future;

public interface Kernel {

    Future<PluginRegistration> install(URL url);
}
