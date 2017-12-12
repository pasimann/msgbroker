package com.pasimann.producer.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pasimann.producer.dataobjects.ApplicationData;
import com.pasimann.producer.crypto.MessageEncoder;

@Service
public class MessageSender {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessageToQueue(ApplicationData data) {
        log.info("Sending message to RabbitMq.");

        byte[] stringbytes = data.getData().getBytes(StandardCharsets.UTF_8);
        String encodedData = MessageEncoder.encodeMessage(stringbytes);

        ApplicationData encoded = new ApplicationData(encodedData, data.getDate());
        encoded.setId(data.getId());

        rabbitTemplate.convertAndSend(
            RabbitMqConfiguration.EXCHANGE_NAME,
            RabbitMqConfiguration.ROUTING_KEY_NAME, encoded);
    }
}

