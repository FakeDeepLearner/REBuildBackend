package com.rebuild.backend.config.rabbitmq;

import com.google.common.base.Throwables;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentReplyLikeRequest;
import com.rebuild.backend.utils.batch.processors.ReplyLikeProcessor;
import com.rebuild.backend.utils.batch.readers.RestartableReplyLikeReader;
import com.rebuild.backend.utils.batch.writers.RepliesWriter;
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
public class ReplyLikeBatchStepsConfig {

    private final RestartableReplyLikeReader restartableReplyLikeReader;

    private final ReplyLikeProcessor replyLikeProcessor;

    private final RepliesWriter repliesWriter;

    @Autowired
    public ReplyLikeBatchStepsConfig(RestartableReplyLikeReader restartableReplyLikeReader,
                                     ReplyLikeProcessor replyLikeProcessor,
                                     RepliesWriter repliesWriter) {
        this.restartableReplyLikeReader = restartableReplyLikeReader;
        this.replyLikeProcessor = replyLikeProcessor;
        this.repliesWriter = repliesWriter;
    }


    @Bean
    public Step commentReplyLikeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("replyLikeStep", jobRepository).
                <CommentReplyLikeRequest, Like>chunk(20, transactionManager).
                reader(restartableReplyLikeReader).
                processor(replyLikeProcessor).
                writer(repliesWriter).
                faultTolerant().skipPolicy((Throwable t, long skipCount) -> {
                    Throwable rootCause = Throwables.getRootCause(t);
                    return rootCause instanceof ConstraintViolationException;
                }).build();
    }

    @Bean
    public Job commentRepliesLikeJob(JobRepository jobRepository, @Qualifier("commentReplyLikeStep") Step postLikeStep) {
        return new JobBuilder("commentRepliesLikeJob", jobRepository).start(postLikeStep).build();
    }
}
