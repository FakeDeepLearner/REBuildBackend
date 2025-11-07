package com.rebuild.backend.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


@Configuration
public class RabbitMQConfig {

    public final static String FRIENDS_QUEUE_NAME = "friendsQueue";

    public final static String COMMENT_QUEUE_NAME = "commentsQueue";

    public final static String POSTS_QUEUE_NAME = "postsQueue";

    public final static String EXCHANGE_NAME = "likesExchange";

    public final static String COMMENT_ROUTING_KEY = "commentLikesRoutingKey";

    public final static String POST_ROUTING_KEY = "postLikesRoutingKey";

    public final static String FRIENDS_ROUTING_KEY = "friendsRoutingKey";

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
    public Queue friendsQueue() {
        return new Queue(FRIENDS_QUEUE_NAME, true);
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
    public Binding friendsBinding(){
        return BindingBuilder.bind(friendsQueue()).to(likesExchange()).with(FRIENDS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;

    }

    @Bean
    public JobOperator jobOperator(JobRepository repository) {
        TaskExecutorJobOperator operator =  new TaskExecutorJobOperator();
        operator.setJobRepository(repository);
        return operator;
    }




}
