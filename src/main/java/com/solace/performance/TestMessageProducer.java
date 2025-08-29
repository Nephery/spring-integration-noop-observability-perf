package com.solace.performance;

import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Test message producer using standard Spring Integration MessageProducerSupport.
 * Uses the default sendMessage() behavior for performance testing.
 */
public class TestMessageProducer extends MessageProducerSupport {

    private final AtomicLong messageCount = new AtomicLong(0);

    /**
     * Public method to send test messages for performance testing.
     */
    public void sendTestMessage(String payload) {
        if (!isRunning()) {
            return;
        }

        Message<String> message = new GenericMessage<>(payload);
        sendMessage(message);
        messageCount.incrementAndGet();
    }

    /**
     * Returns the count of messages sent during testing.
     */
    public long getMessageCount() {
        return messageCount.get();
    }

    /**
     * Resets the message counter to zero.
     */
    public void resetCount() {
        messageCount.set(0);
    }

    @Override
    public String getComponentType() {
        return "test-message-producer";
    }
}