package com.rebuild.backend.config.rabbitmq;

import com.rebuild.backend.model.forms.dtos.forum_dtos.PostLikeRequest;
import com.rebuild.backend.utils.batch.processors.PostLikeProcessor;
import com.rebuild.backend.utils.batch.readers.RestartablePostLikeReader;
import com.rebuild.backend.utils.batch.writers.PostLikesWriter;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class PostLikeBatchStepsConfig {

}
