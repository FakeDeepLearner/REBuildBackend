package com.rebuild.backend.config.rabbitmq;

import com.google.common.base.Throwables;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import com.rebuild.backend.batch.processors.PostLikeProcessor;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.Step;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.amqp.AmqpItemReader;
import org.springframework.batch.item.amqp.builder.AmqpItemReaderBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PostLikeBatchStepsConfig {

    private final EntityManagerFactory entityManagerFactory;

    private final RabbitTemplate rabbitTemplate;

    private final PostLikeProcessor postLikeProcessor;

    @Autowired
    public PostLikeBatchStepsConfig(EntityManagerFactory entityManagerFactory,
                                    RabbitTemplate rabbitTemplate,
                                    PostLikeProcessor postLikeProcessor) {
        this.entityManagerFactory = entityManagerFactory;
        this.rabbitTemplate = rabbitTemplate;
        this.postLikeProcessor = postLikeProcessor;
    }

    @Bean(name = "postsReader")
    @StepScope
    public AmqpItemReader<PostLikeRequest> reader() {
        return new AmqpItemReaderBuilder<PostLikeRequest>().
                amqpTemplate(rabbitTemplate).itemType(PostLikeRequest.class).build();
    }


    @Bean(name = "postLikesWriter")
    @StepScope
    public JpaItemWriter<Like> writer() {
        return new JpaItemWriterBuilder<Like>().
                entityManagerFactory(entityManagerFactory).build();
    }

    @Bean
    public Step postLikeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, AmqpItemReader<PostLikeRequest> postsReader, JpaItemWriter<Like> postLikesWriter) {
        return new StepBuilder("postLikeStep", jobRepository).
                <PostLikeRequest, Like>chunk(20).
                reader(postsReader).
                processor(postLikeProcessor).
                writer(postLikesWriter).
                faultTolerant().skipPolicy((Throwable t, long skipCount) -> {
                    Throwable rootCause = Throwables.getRootCause(t);
                    return rootCause instanceof ConstraintViolationException;
                }).build();
    }

    @Bean
    public Job postLikeJob(JobRepository jobRepository, @Qualifier("postLikeStep") Step postLikeStep) {
        return new JobBuilder("postLikeJob", jobRepository).start(postLikeStep).
                incrementer(new RunIdIncrementer()).
                build();
    }

}
