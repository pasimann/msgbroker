package com.pasimann.consumer.rabbitmq;

public class ConsumerException extends Exception {
    static final long serialVersionUID = 1L;

    public ConsumerException(String s) {
        super(s);
    }

    public ConsumerException(Exception e) {
        super(e);
    }
}
