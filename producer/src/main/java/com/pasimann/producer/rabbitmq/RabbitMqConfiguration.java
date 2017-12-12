package com.pasimann.producer.rabbitmq;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class RabbitMqConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfiguration.class);

    static final String EXCHANGE_NAME       = "pasimann-data.exchange";

    static final String ROUTING_KEY_NAME    = "pasimann-data";
    static final String INCOMING_QUEUE_NAME = "pasimann-data.incoming.queue";

    static final String DEAD_LETTER_QUEUE_NAME = "pasimann-data.dead-letter.queue";
    static final int DEAD_LETTER_QUEUE_TTL     = 1800000;

    private final static String PROPERTIES_FILENAME = "PRODUCER";
    private static ResourceBundle propertiesBundle;

    static {
       propertiesBundle = ResourceBundle.getBundle(PROPERTIES_FILENAME);
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue incomingQueue() {
        return QueueBuilder.durable(INCOMING_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE_NAME)
                .build();
    }

    @Bean
    Binding binding() {
        return BindingBuilder.bind(incomingQueue()).to(exchange()).with(ROUTING_KEY_NAME);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_NAME)
                .withArgument("x-message-ttl", getDeadLetterExchangeTtl())
                .build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setRoutingKey(INCOMING_QUEUE_NAME);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        String hostName = propertiesBundle.getString("producer.rabbitmqhost");

        if (System.getenv("SPRING_RABBITMQ_HOST") != null) {
			hostName = System.getenv("SPRING_RABBITMQ_HOST");
		}

        // only the changed password is read from the env in production; username is always guest
        String username = propertiesBundle.getString("producer.rabbitmquser");
        String password = propertiesBundle.getString("producer.rabbitmqpasswd");

        if (System.getenv("SPRING_RABBITMQ_PASSWORD") != null) {
			password = System.getenv("SPRING_RABBITMQ_PASSWORD");
		}

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(hostName);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        log.info("Connection to host {}", hostName);

        return connectionFactory;
    }

    private int getDeadLetterExchangeTtl() {
        int time = Integer.parseInt(
            propertiesBundle.getString("producer.dlxtimetolive"));

        if (System.getenv("SPRING_RABBIT_DLX_TTL") != null) {
            time = Integer.parseInt(System.getenv("SPRING_RABBIT_DLX_TTL"));
        }
        return time;
    }
}