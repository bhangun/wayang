package tech.kayys.wayang.messaging;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import tech.kayys.wayang.extension.Extension;

/**
 * Message Queue Service
 */
public interface MessageQueueService extends Extension {
    
    // Publish
    void publish(String topic, Message message) throws Exception;
    void publish(String topic, Message message, Map<String, String> headers) throws Exception;
    void publishBatch(String topic, List<Message> messages) throws Exception;
    
    // Subscribe
    void subscribe(String topic, MessageListener listener) throws Exception;
    void subscribe(String topic, String group, MessageListener listener) throws Exception;
    void unsubscribe(String topic, MessageListener listener) throws Exception;
    
    // Consumer management
    void pauseConsumer(String topic) throws Exception;
    void resumeConsumer(String topic) throws Exception;
    boolean isConsumerRunning(String topic) throws Exception;
    
    // Admin
    void createTopic(String topic, int partitions, int replicationFactor) throws Exception;
    void deleteTopic(String topic) throws Exception;
    List<String> listTopics() throws Exception;
    TopicInfo getTopicInfo(String topic) throws Exception;
    
    // Health
    boolean isHealthy();
}