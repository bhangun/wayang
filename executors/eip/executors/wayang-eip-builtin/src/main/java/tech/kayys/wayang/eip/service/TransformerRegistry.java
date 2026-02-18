package tech.kayys.wayang.eip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.eip.strategy.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TransformerRegistry {

    private final Map<String, MessageTransformer> transformers = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @PostConstruct
    void init() {
        transformers.put("uppercase", new UppercaseTransformer());
        transformers.put("lowercase", new LowercaseTransformer());
        transformers.put("trim", new TrimTransformer());
        transformers.put("json-to-map", new JsonToMapTransformer(objectMapper));
        transformers.put("map-to-json", new MapToJsonTransformer(objectMapper));
        transformers.put("base64-encode", new Base64EncodeTransformer());
        transformers.put("base64-decode", new Base64DecodeTransformer());
    }

    public MessageTransformer getTransformer(String type) {
        return transformers.getOrDefault(type, new IdentityTransformer());
    }

    public void registerTransformer(String type, MessageTransformer transformer) {
        transformers.put(type, transformer);
    }
}
