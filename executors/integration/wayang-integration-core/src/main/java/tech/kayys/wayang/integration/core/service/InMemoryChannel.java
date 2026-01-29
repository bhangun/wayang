package tech.kayys.wayang.integration.core.service;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.integration.core.model.ChannelMessage;
import tech.kayys.wayang.integration.core.model.MessageChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class InMemoryChannel implements MessageChannel {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryChannel.class);

    private final String name;
    private final BlockingQueue<ChannelMessage> queue = new LinkedBlockingQueue<>(1000);

    public InMemoryChannel(String name) {
        this.name = name;
    }

    @Override
    public Uni<String> send(Object message) {
        return Uni.createFrom().item(() -> {
            String messageId = UUID.randomUUID().toString();
            ChannelMessage msg = new ChannelMessage(messageId, message, Instant.now());

            if (!queue.offer(msg)) {
                throw new RuntimeException("Channel " + name + " is full");
            }

            LOG.debug("Message sent to channel {}: {}", name, messageId);
            return messageId;
        });
    }

    @Override
    public Uni<Object> receive() {
        return Uni.createFrom().item(() -> {
            try {
                ChannelMessage msg = queue.poll(5, TimeUnit.SECONDS);
                if (msg != null) {
                    LOG.debug("Message received from channel {}: {}", name, msg.id());
                    return msg.payload();
                }
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Uni<Object> peek() {
        return Uni.createFrom().item(() -> {
            ChannelMessage msg = queue.peek();
            return msg != null ? msg.payload() : null;
        });
    }

    @Override
    public Uni<Long> size() {
        return Uni.createFrom().item((long) queue.size());
    }
}
