package io.sunshower.kernel;

public interface KernelListener {

    default void onStopRequested() {
    }

    default void onStartRequested() {
    }

    default void onRestartRequested() {
    }

}
