package com.rebuild.backend.config.rabbitmq;

import com.google.common.base.Throwables;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentLikeRequest;
import com.rebuild.backend.model.forms.dtos.forum_dtos.FriendRequestDTO;
import com.rebuild.backend.utils.batch.processors.FriendsProcessor;
import com.rebuild.backend.utils.batch.readers.FriendsReader;
import com.rebuild.backend.utils.batch.writers.FriendsWriter;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FriendshipsBatchStepsConfig {

    private final FriendsReader friendsReader;

    private final FriendsProcessor friendsProcessor;

    private final FriendsWriter friendsWriter;

    @Autowired
    public FriendshipsBatchStepsConfig(FriendsReader friendsReader,
                                       FriendsProcessor friendsProcessor, FriendsWriter friendsWriter) {
        this.friendsReader = friendsReader;
        this.friendsProcessor = friendsProcessor;
        this.friendsWriter = friendsWriter;
    }


    @Bean
    public Step friendLikeStep(JobRepository jobRepository) {
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
        return new JobBuilder("friendLikeJob", jobRepository).start(friendLikeStep).build();
    }
}
