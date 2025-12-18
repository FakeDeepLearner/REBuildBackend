package com.rebuild.backend.config.rabbitmq;

import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.batch.processors.FriendsProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.amqp.AmqpItemReader;
import org.springframework.batch.item.amqp.builder.AmqpItemReaderBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FriendshipsBatchStepsConfig {

    private final EntityManagerFactory entityManagerFactory;

    private final RabbitTemplate rabbitTemplate;

    private final FriendsProcessor friendsProcessor;

    @Autowired
    public FriendshipsBatchStepsConfig(EntityManagerFactory entityManagerFactory,
                                       RabbitTemplate rabbitTemplate,
                                       FriendsProcessor friendsProcessor) {
        this.entityManagerFactory = entityManagerFactory;
        this.rabbitTemplate = rabbitTemplate;
        this.friendsProcessor = friendsProcessor;
    }

    @Bean(name = "friendsReader")
    @StepScope
    public AmqpItemReader<FriendRequestDTO> reader() {
        return new AmqpItemReaderBuilder<FriendRequestDTO>().
                amqpTemplate(rabbitTemplate).itemType(FriendRequestDTO.class).build();
    }

    @Bean(name = "friendsWriter")
    @StepScope
    public JpaItemWriter<FriendRequest> writer() {
        return new JpaItemWriterBuilder<FriendRequest>().
                entityManagerFactory(entityManagerFactory).build();
    }


    @Bean
    public Step friendLikeStep(JobRepository jobRepository, AmqpItemReader<FriendRequestDTO> friendsReader,
                               JpaItemWriter<FriendRequest> friendsWriter) {
        return new StepBuilder("commentLikeStep", jobRepository).
                <FriendRequestDTO, FriendRequest>chunk(100).
                reader(friendsReader).
                processor(friendsProcessor).
                writer(friendsWriter).
                faultTolerant().
                skip(NullPointerException.class).
                skipLimit(100).
                build();
    }


    @Bean
    public Job friendLikeJob(JobRepository jobRepository, @Qualifier("friendLikeStep") Step friendLikeStep) {
        return new JobBuilder("friendLikeJob", jobRepository).start(friendLikeStep).
                incrementer(new RunIdIncrementer()).
                build();
    }
}
