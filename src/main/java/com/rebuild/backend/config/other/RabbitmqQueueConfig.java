package com.rebuild.backend.config.other;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitmqQueueConfig {

    private final static String COMMENT_QUEUE_NAME = "likesQueue";

    private final static String POSTS_QUEUE_NAME = "postsQueue";

    private final static String EXCHANGE_NAME = "likesExchange";

    private final static String COMMENT_ROUTING_KEY = "commentLikesRoutingKey";

    private final static String POST_ROUTING_KEY = "postLikesRoutingKey";

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

}
