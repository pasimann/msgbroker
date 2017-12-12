package com.pasimann.consumer.rabbitmq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

import com.pasimann.consumer.dataobjects.ApplicationData;
import com.pasimann.consumer.rabbitmq.ConsumerException;
import com.pasimann.consumer.crypto.MessageDecoder;

@Service
public class MessageConsumer implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private long getRetryCountFromMessageHeaders(final Map<String, Object> headers) {
        long result = 0;

        if (headers != null) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (key.equals("count")) {
                    return (Long)value;
                }

                if (key.equals("x-death")) {
                    List<Object> death =
                        (value instanceof List ? (List<Object>)value : null);
                    if (death != null) {
                        for (Object o : death) {
                            Map<String, Object> deathHeaders =
                                (o instanceof Map ? (Map<String, Object>)o : null);
                            result = getRetryCountFromMessageHeaders(deathHeaders);
                        }
                    }
                }
            }
        }

        return result;
    }

    private void messageToDeadLetterExchange(String message, String error) {
        log.error("Sending message to DLX due exceptions {}", message);
        throw new AmqpRejectAndDontRequeueException(error);
    }

    @Override
    public void onMessage(Message message) {
        String jsonBody = new String(message.getBody());
        log.info("Consuming data {}", jsonBody);

        try {

            MessageProperties props = message.getMessageProperties();
            Map<String, Object> headers = props.getHeaders();

            long retryCount = this.getRetryCountFromMessageHeaders(headers);

            if (retryCount > 0) {
                log.info("Message retry no {}", retryCount);
            }

            ApplicationData data = MessageDecoder.getDecodedApplicationData(jsonBody);
            data.setRetryCount(retryCount);

            log.info("Message object {}", data.toString());

            // TODO do something with the data

            return;

        } catch (ConsumerException e) {
            log.error("Message send error caught.", e);
            messageToDeadLetterExchange(jsonBody, e.getMessage());

        } catch (Exception e) {
            log.error("Fatal error caught when sending message.", e);
            messageToDeadLetterExchange(jsonBody, e.getMessage());
        }

        latch.countDown();

    }

    public CountDownLatch getLatch() {
        return latch;
    }

}