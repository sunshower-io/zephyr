package io.sunshower.common.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MonitorableByteChannel implements ReadableByteChannel {

    private final long                expectedSize;
    private final Listener            listener;
    private final ReadableByteChannel delegate;


    private long read;


    public MonitorableByteChannel(
            final ReadableByteChannel delegate,
            final Listener listener,
            long expectedSize
    ) {
        this.listener = listener;
        this.delegate = delegate;
        this.expectedSize = expectedSize;
    }


    @Override
    public int read(ByteBuffer destination) throws IOException {
        int    n;
        double progress;
        if ((n = delegate.read(destination)) > 0) {
            read += n;
            System.out.println(expectedSize);
            progress = expectedSize > 0 ?
                    ((double) read / (double) expectedSize) * 100.0 : -1.0;
            listener.onTransfer(this, progress);
        }
        return n;
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public interface Listener {

        default void onTransfer(ReadableByteChannel channel, double progress) {

        }

        default void onComplete(ReadableByteChannel channel) {

        }

        default void onError(ReadableByteChannel channel, Exception ex) {

        }

        default void onCancel(ReadableByteChannel channel) {

        }
    }
}
