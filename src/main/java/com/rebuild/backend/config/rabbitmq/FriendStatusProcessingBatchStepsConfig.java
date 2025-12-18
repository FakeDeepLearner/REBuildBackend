package com.rebuild.backend.config.rabbitmq;

import com.rebuild.backend.batch.writers.FriendStatusUpgradeWriter;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.batch.readers.FriendRequestsReader;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
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
public class FriendStatusProcessingBatchStepsConfig {


    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public FriendStatusProcessingBatchStepsConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean(name = "requestsReader")
    @StepScope
    public ItemReader<FriendRequest> requestsReader() throws Exception {

        Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);

        return new FriendRequestsReader(entityManagerFactory, cutoff);

    }


    @Bean
    public Step friendStatusStep(JobRepository jobRepository,
                                 ItemReader<FriendRequest> requestsReader,
                                 FriendStatusUpgradeWriter likesWriter) throws Exception {
        return new StepBuilder("friendStatusStep", jobRepository).
                <FriendRequest, FriendRequest>chunk(100).
                reader(requestsReader).writer(likesWriter).faultTolerant().
                skip(NoSuchElementException.class).skipLimit(50).build();

    }

    @Bean
    public Job friendStatusJob(JobRepository jobRepository, @Qualifier("friendStatusStep") Step statusStep) {
        return new JobBuilder("friendStatusJob", jobRepository).start(statusStep).
                incrementer(new RunIdIncrementer()).
                build();
    }
}
