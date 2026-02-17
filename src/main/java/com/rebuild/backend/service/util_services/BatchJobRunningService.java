package com.rebuild.backend.service.util_services;

import com.rebuild.backend.batch.BatchJobExecutor;
import org.springframework.batch.core.job.parameters.JobParametersInvalidException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BatchJobRunningService {

    private final BatchJobExecutor jobExecutor;

    @Autowired
    public BatchJobRunningService(BatchJobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    //Every 10 seconds
    @Scheduled(fixedRate = 10 * 1000)
    public void runFriendsUpdatingJob()
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException {

        jobExecutor.executeJob("friendLikeJob");
    }

    @Scheduled(cron = "@midnight")
    public void runFriendRequestsJob()
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException
    {
        jobExecutor.executeJob("friendStatusJob");

    }


    //Every minute
    @Scheduled(fixedRate = 60 * 1000)
    public void runLikesUpdatingJob() throws JobInstanceAlreadyCompleteException, NoSuchJobException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        jobExecutor.executeJob("updateLikesJob");

    }

    //Every 15 seconds
    @Scheduled(fixedRate = 15 * 1000)
    public void runLikesProcessingJobs()
            throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException, NoSuchJobException {

        jobExecutor.executeJob("postLikeJob");
        jobExecutor.executeJob("commentLikeJob");

    }
}
