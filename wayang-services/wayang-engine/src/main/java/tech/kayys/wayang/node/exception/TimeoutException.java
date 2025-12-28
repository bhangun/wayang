package tech.kayys.wayang.node.exception;

class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }
}