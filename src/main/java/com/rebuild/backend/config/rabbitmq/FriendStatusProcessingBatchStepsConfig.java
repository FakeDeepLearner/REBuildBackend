package com.rebuild.backend.config.rabbitmq;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import com.rebuild.backend.utils.batch.readers.FriendRequestsReader;
import com.rebuild.backend.utils.batch.readers.LikeUpdateBatchReader;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Configuration
public class FriendStatusProcessingBatchStepsConfig {

    @Bean
    @StepScope
    public ItemReader<FriendRequest> requestsReader(EntityManagerFactory entityManagerFactory,
                                                 @Value("#{jobParameters['dateCutoff']}") String cutoffTime) throws Exception {
        LocalDateTime timeCutoff = LocalDateTime.parse(cutoffTime);

        return new FriendRequestsReader(entityManagerFactory, timeCutoff);

    }

    @Bean
    public Step friendStatusStep(JobRepository jobRepository,
                                  @Qualifier(value = "requestsReader") ItemReader<FriendRequest> requestsReader,
                                  @Qualifier(value = "friendStatusUpgradeWriter") ItemWriter<FriendRequest> likesWriter) throws Exception {
        return new StepBuilder("friendStatusStep", jobRepository).
                <FriendRequest, FriendRequest>chunk(100).
                reader(requestsReader).writer(likesWriter).faultTolerant().
                skip(NoSuchElementException.class).skipLimit(50).build();

    }

    @Bean
    public Job friendStatusJob(JobRepository jobRepository, @Qualifier("friendStatusStep") Step statusStep) {
        return new JobBuilder("friendStatusJob", jobRepository).start(statusStep).
                build();
    }
}
