package io.sunshower.common.io;

import io.sunshower.kernel.io.ChannelTransferListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MonitorableByteChannel implements ReadableByteChannel {

    private final long                    expectedSize;
    private final ChannelTransferListener listener;
    private final ReadableByteChannel     delegate;


    private long read;


    public MonitorableByteChannel(
            final ReadableByteChannel delegate,
            final ChannelTransferListener listener,
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
}
