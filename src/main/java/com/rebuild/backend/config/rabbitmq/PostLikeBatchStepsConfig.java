package com.rebuild.backend.config.rabbitmq;

import com.rebuild.backend.config.properties.BatchChunkSize;
import com.rebuild.backend.model.entities.forum_entities.PostLike;
import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import com.rebuild.backend.utils.batch.processors.PostLikeProcessor;
import com.rebuild.backend.utils.batch.readers.RestartablePostLikeReader;
import com.rebuild.backend.utils.batch.writers.PostLikesWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PostLikeBatchStepsConfig {

    private final RestartablePostLikeReader postLikeReader;

    private final PostLikeProcessor postLikeProcessor;

    private final PostLikesWriter postLikesWriter;

    private final BatchChunkSize chunkSize;

    @Autowired
    public PostLikeBatchStepsConfig(RestartablePostLikeReader postLikeReader,
                                    PostLikeProcessor postLikeProcessor,
                                    PostLikesWriter postLikesWriter, BatchChunkSize chunkSize) {
        this.postLikeReader = postLikeReader;
        this.postLikeProcessor = postLikeProcessor;
        this.postLikesWriter = postLikesWriter;
        this.chunkSize = chunkSize;
    }

    @Bean
    public Step postLikeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("postLikeStep", jobRepository).
                <PostLikeRequest, PostLike>chunk(chunkSize.size(), transactionManager).
                reader(postLikeReader).
                processor(postLikeProcessor).
                writer(postLikesWriter).
                build();
    }

    @Bean
    public Job postLikeJob(JobRepository jobRepository, @Qualifier("postLikeStep") Step postLikeStep) {
        return new JobBuilder("postLikeJob", jobRepository).start(postLikeStep).
                build();
    }

}
