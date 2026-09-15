package tech.kayys.wayang.communication.message;

public interface MessagePayload {

    String mediaType();

    Object value();

    static MessagePayload of(String mediaType, Object value) {
        return new DefaultMessagePayload(mediaType, value);
    }

    static MessagePayload text(String text) {
        return new DefaultMessagePayload("text/plain", text);
    }

    static MessagePayload json(Object json) {
        return new DefaultMessagePayload("application/json", json);
    }
}
