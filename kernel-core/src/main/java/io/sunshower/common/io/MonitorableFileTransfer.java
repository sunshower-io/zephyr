package io.sunshower.common.io;

import lombok.val;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MonitorableFileTransfer implements Callable<File>, MonitorableByteChannel.Listener {

    private final long                                  expectedSize;
    private final File                                  destination;
    private final ReadableByteChannel                   channel;
    private final List<MonitorableByteChannel.Listener> listeners;

    private final Object lock = new Object();


    public MonitorableFileTransfer(
            File destination,
            long expectedSize,
            ReadableByteChannel channel
    ) {

        this.listeners = new LinkedList<>();
        this.channel = channel;
        this.destination = destination;
        this.expectedSize = expectedSize;
    }

    public void addListener(MonitorableByteChannel.Listener listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void removeListener(MonitorableByteChannel.Listener listener) {
        synchronized (lock) {
            listeners.removeIf(t -> t == listener);
        }
    }

    @Override
    public File call() throws Exception {
        Exception e      = null;
        val       source = new MonitorableByteChannel(channel, this, expectedSize);
        try (val outputStream = new FileOutputStream(destination);
             val destination = outputStream.getChannel()) {
            destination.transferFrom(source, 0, Long.MAX_VALUE);
        } catch (ClosedChannelException ex) {
            e = ex;
            onCancel(channel);
        } catch (IOException ex) {
            e = ex;
            onError(channel, ex);
        }
        if (e != null) {
            throw e;
        }
        onComplete(channel);
        return destination;
    }

    @Override
    public void onTransfer(ReadableByteChannel channel, double progress) {
        System.out.println(progress);
        synchronized (lock) {
            for (MonitorableByteChannel.Listener listener : listeners) {
                listener.onTransfer(channel, progress);
            }
        }

    }

    @Override
    public void onComplete(ReadableByteChannel channel) {
        synchronized (lock) {
            for (MonitorableByteChannel.Listener listener : listeners) {
                listener.onComplete(channel);
            }
        }
    }

    @Override
    public void onError(ReadableByteChannel channel, Exception ex) {
        synchronized (lock) {
            for (MonitorableByteChannel.Listener listener : listeners) {
                listener.onError(channel, ex);
            }
        }
    }

    @Override
    public void onCancel(ReadableByteChannel channel) {
        synchronized (lock) {
            for (MonitorableByteChannel.Listener listener : listeners) {
                listener.onCancel(channel);
            }
        }

    }
}
