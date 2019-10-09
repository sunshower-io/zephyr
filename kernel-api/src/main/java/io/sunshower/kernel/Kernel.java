package io.sunshower.kernel;

public interface Kernel {

    enum State {
        /**
         *  Kernel modules are loaded, plugins are unloaded
         */
        Passive,

        /**
         *
         * Plugins are loaded, kernel modules are loaded
         */
        Running,

        /**
         * Kernel's in an unknown (bad) state, but can't report its exact error
         */
        Unknown,
        /**
         * Kernel's in an error state
         */
        Error,

        /**
         * Kernel is shutting down
         */
        Stopping,

        /**
         * Kernel is starting up
         */
        Starting,
        /**
         * Kernel is restarting
         */
        Restarting
    }

    /**
     *
     * @return this kernel instance's plugin manager
     */

    PluginManager getPluginManager();

    /**
     *
     * @return this kernel instance's module manager
     */
    KernelModuleManager getModuleManager();


    /**
     *
     * @return this kernel instance's lifecycle manager
     */
    LifecycleManager getLifecycleManager();



}
