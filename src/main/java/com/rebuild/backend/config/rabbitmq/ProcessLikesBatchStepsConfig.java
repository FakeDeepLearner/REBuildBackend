package com.rebuild.backend.config.rabbitmq;


import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import com.rebuild.backend.utils.batch.readers.LikeUpdateBatchReader;
import com.rebuild.backend.utils.batch.writers.LikeUpdateWriter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
public class ProcessLikesBatchStepsConfig {

    private final LikeUpdateWriter likeUpdateWriter;

    @Autowired
    public ProcessLikesBatchStepsConfig(LikeUpdateWriter likeUpdateWriter) {
        this.likeUpdateWriter = likeUpdateWriter;
    }


    @Bean
    @StepScope
    public ItemReader<LikesUpdateDTO> likesReader(EntityManagerFactory entityManagerFactory,
                                                  @Value("#{jobParameters['lastProcessed']}") String lastProcessedTime) throws Exception {
        LocalDateTime cutoffTime = LocalDateTime.parse(lastProcessedTime);

        return new LikeUpdateBatchReader(entityManagerFactory, cutoffTime);

    }

    @Bean
    public Step likesUpdatingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                  @Qualifier(value = "likesReader") ItemReader<LikesUpdateDTO> likesReader) {
        return new StepBuilder("updateLikesStep", jobRepository).
                <LikesUpdateDTO, LikesUpdateDTO>chunk(50, transactionManager).
                reader(likesReader).writer(likeUpdateWriter).build();

     }

    @Bean
    public Job updateLikesJob(JobRepository jobRepository, @Qualifier("likesUpdatingStep") Step likesStep) {
        return new JobBuilder("likesUpdatingJob", jobRepository).start(likesStep).
                build();
    }


}
