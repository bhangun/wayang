package tech.kayys.wayang.eip.service;

import tech.kayys.wayang.eip.model.MessageChannel;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ChannelRegistry {

    private final ConcurrentHashMap<String, MessageChannel> channels = new ConcurrentHashMap<>();

    public MessageChannel getChannel(String name) {
        return channels.computeIfAbsent(name, InMemoryChannel::new);
    }

    public void registerChannel(String name, MessageChannel channel) {
        channels.put(name, channel);
    }
}
