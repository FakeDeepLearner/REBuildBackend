package com.rebuild.backend.config.rabbitmq;

import com.google.common.base.Throwables;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.batch.processors.CommentLikeProcessor;
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
import org.springframework.batch.item.amqp.AmqpItemWriter;
import org.springframework.batch.item.amqp.builder.AmqpItemReaderBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CommentLikeBatchStepsConfig {

    private final RabbitTemplate rabbitTemplate;

    private final EntityManagerFactory entityManagerFactory;

    private final CommentLikeProcessor likeProcessor;


    @Autowired
    public CommentLikeBatchStepsConfig(RabbitTemplate rabbitTemplate,
                                       EntityManagerFactory entityManagerFactory,
                                       CommentLikeProcessor likeProcessor) {
        this.rabbitTemplate = rabbitTemplate;
        this.entityManagerFactory = entityManagerFactory;
        this.likeProcessor = likeProcessor;
    }

    @Bean(name = "likeReader")
    @StepScope
    public AmqpItemReader<CommentLikeRequest> reader() {
        return new AmqpItemReaderBuilder<CommentLikeRequest>().
                amqpTemplate(rabbitTemplate).itemType(CommentLikeRequest.class).build();
    }

    @Bean(name = "commentsWriter")
    @StepScope
        public JpaItemWriter<Like> writer() {
        return new JpaItemWriterBuilder<Like>().
                entityManagerFactory(entityManagerFactory).build();
    }

    @Bean
    public Step commentLikeStep(JobRepository jobRepository,
                                AmqpItemReader<CommentLikeRequest> likeReader, JpaItemWriter<Like> commentsWriter) {
        return new StepBuilder("commentLikeStep", jobRepository).
                <CommentLikeRequest, Like>chunk(20).
                reader(likeReader).
                processor(likeProcessor).
                writer(commentsWriter).
                faultTolerant().skipPolicy((Throwable t, long skipCount) -> {
                    Throwable rootCause = Throwables.getRootCause(t);
                    return rootCause instanceof ConstraintViolationException;
                }).build();
    }

    @Bean
    public Job commentLikeJob(JobRepository jobRepository, @Qualifier("commentLikeStep") Step postLikeStep) {
        return new JobBuilder("commentLikeJob", jobRepository).start(postLikeStep).
                incrementer(new RunIdIncrementer()).
                build();
    }
}
