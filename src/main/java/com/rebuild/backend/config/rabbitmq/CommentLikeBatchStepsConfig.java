package com.rebuild.backend.config.rabbitmq;

import com.google.common.base.Throwables;
import com.rebuild.backend.config.properties.BatchChunkSize;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.utils.batch.processors.CommentLikeProcessor;
import com.rebuild.backend.utils.batch.readers.RestartableCommentLikeReader;
import com.rebuild.backend.utils.batch.writers.CommentsWriter;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class CommentLikeBatchStepsConfig {

    private final RestartableCommentLikeReader likeReader;

    private final CommentsWriter commentsWriter;

    private final CommentLikeProcessor likeProcessor;

    private final BatchChunkSize chunkSize;

    @Autowired
    public CommentLikeBatchStepsConfig(RestartableCommentLikeReader likeReader,
                                       CommentsWriter commentsWriter,
                                       CommentLikeProcessor likeProcessor, BatchChunkSize chunkSize) {
        this.likeReader = likeReader;
        this.commentsWriter = commentsWriter;
        this.likeProcessor = likeProcessor;
        this.chunkSize = chunkSize;
    }

    @Bean
    public Step commentLikeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("commentLikeStep", jobRepository).
                <CommentLikeRequest, Like>chunk(chunkSize.size(), transactionManager).
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
        return new JobBuilder("commentLikeJob", jobRepository).start(postLikeStep).build();
    }
}
