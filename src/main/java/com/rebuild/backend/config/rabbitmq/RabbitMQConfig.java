package com.rebuild.backend.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMQConfig {

    private final static String COMMENT_QUEUE_NAME = "likesQueue";

    private final static String POSTS_QUEUE_NAME = "postsQueue";

    private final static String EXCHANGE_NAME = "likesExchange";

    private final static String COMMENT_ROUTING_KEY = "commentLikesRoutingKey";

    private final static String POST_ROUTING_KEY = "postLikesRoutingKey";

    @Bean
    public ConnectionFactory cachingFactory(){
        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public Queue postLikesQueue() {
        return new Queue(POSTS_QUEUE_NAME, true);
    }

    @Bean
    public Queue commentLikesQueue() {
        return new Queue(COMMENT_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange likesExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding commentLikesBinding() {
        return BindingBuilder.bind(commentLikesQueue()).to(likesExchange()).with(COMMENT_ROUTING_KEY);
    }

    @Bean
    public Binding postLikesBinding() {
        return BindingBuilder.bind(postLikesQueue()).to(likesExchange()).with(POST_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;

    }




}
