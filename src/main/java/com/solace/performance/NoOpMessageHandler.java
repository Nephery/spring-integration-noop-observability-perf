package com.solace.performance;

import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple message handler that does nothing with received messages,
 * used for performance testing to isolate the observability overhead.
 */
public class NoOpMessageHandler extends AbstractMessageHandler {

    private final AtomicLong receivedCount = new AtomicLong(0);

    @Override
    protected void handleMessageInternal(Message<?> message) {
        // Do nothing - this is a no-op handler for performance testing
        receivedCount.incrementAndGet();
    }

    public long getReceivedCount() {
        return receivedCount.get();
    }

    public void resetCount() {
        receivedCount.set(0);
    }
}