package com.pasimann.consumer;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import com.pasimann.consumer.rabbitmq.MessageConsumer;

@Configuration
public class ConsumerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ConsumerConfiguration.class);

    static final String INCOMING_QUEUE_NAME    = "pasimann-data.incoming.queue";
    static final String DEAD_LETTER_QUEUE_NAME = "pasimann-data.dead-letter.queue";

    private final static String PROPERTIES_FILENAME = "CONSUMER";
    private static ResourceBundle propertiesBundle;

    static {
       propertiesBundle = ResourceBundle.getBundle(PROPERTIES_FILENAME);
    }

    @Bean
    Queue incomingQueue() {
        return QueueBuilder.durable(INCOMING_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE_NAME)
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
        String hostName = propertiesBundle.getString("consumer.rabbitmqhost");

        if (System.getenv("SPRING_RABBITMQ_HOST") != null) {
			hostName = System.getenv("SPRING_RABBITMQ_HOST");
		}

        // only the changed password is read from the env in production; username is always guest
        String username = propertiesBundle.getString("consumer.rabbitmquser");
        String password = propertiesBundle.getString("consumer.rabbitmqpasswd");

        if (System.getenv("SPRING_RABBITMQ_PASSWORD") != null) {
			password = System.getenv("SPRING_RABBITMQ_PASSWORD");
		}

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(hostName);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        return connectionFactory;
    }

    @Bean
    public SimpleMessageListenerContainer container() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        ConnectionFactory connectionFactory = connectionFactory();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(INCOMING_QUEUE_NAME);
        container.setMessageListener(getMessageListener());
        container.setMessageConverter(jsonMessageConverter());
        container.setConcurrentConsumers(getNumberOfConsumers());
        container.setMaxConcurrentConsumers(getMaxConcurrentConsumers());

        log.info("Hostname {}", connectionFactory.getHost());
        log.info("Port {}", connectionFactory.getPort());

        return container;
    }

    @Bean
    public MessageListener getMessageListener() {
        return new MessageConsumer();
    }

    private int getNumberOfConsumers() {
        int concurrentConsumers = 1;

        if (System.getenv("NO_OF_CONSUMERS") != null) {
			concurrentConsumers = Integer.parseInt(
                System.getenv("NO_OF_CONSUMERS"));
		} else {
            concurrentConsumers = Integer.parseInt(
                propertiesBundle.getString("consumer.numofconsumers"));
        }
        return concurrentConsumers;
    }

    private int getMaxConcurrentConsumers() {
        int maxConcurrentConsumers = 1;

        if (System.getenv("MAX_CONSUMERS") != null) {
			maxConcurrentConsumers = Integer.parseInt(
                System.getenv("MAX_CONSUMERS"));
		} else {
            maxConcurrentConsumers = Integer.parseInt(
                propertiesBundle.getString("consumer.maxconsumers"));
        }
        return maxConcurrentConsumers;
    }

}