package com.rebuild.backend.config.rabbitmq;


import com.rebuild.backend.batch.writers.LikeUpdateWriter;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import com.rebuild.backend.batch.readers.LikeUpdateBatchReader;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

@Configuration
public class ProcessLikesBatchStepsConfig {

    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public ProcessLikesBatchStepsConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean(name = "likesReader")
    @StepScope
    public ItemReader<LikesUpdateDTO> likesReader() throws Exception {

        Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);


        return new LikeUpdateBatchReader(entityManagerFactory, cutoff);

    }

    @Bean
    public Step likesUpdatingStep(JobRepository jobRepository,
                                  ItemReader<LikesUpdateDTO> likesReader,
                                  LikeUpdateWriter likeUpdateWriter) throws Exception {
            return new StepBuilder("updateLikesStep", jobRepository).
                <LikesUpdateDTO, LikesUpdateDTO>chunk(50).
                reader(likesReader).writer(likeUpdateWriter).faultTolerant().
                skip(NoSuchElementException.class).skipLimit(50).build();

    }

    @Bean
    public Job updateLikesJob(JobRepository jobRepository, @Qualifier("likesUpdatingStep") Step likesStep) {
        return new JobBuilder("updateLikesJob", jobRepository).start(likesStep).
                incrementer(new RunIdIncrementer()).
                build();
    }


}
