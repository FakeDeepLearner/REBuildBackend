package com.rebuild.backend.batch;


import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.job.parameters.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class BatchJobExecutor {

    private final JobRegistry jobRegistry;

    private final JobOperator jobOperator;

    private static final JobParameters EMPTY_PARAMETERS = new JobParametersBuilder().toJobParameters();

    public BatchJobExecutor(@Qualifier("jobRegistry") JobRegistry jobRegistry,
                            JobOperator jobOperator) {
        this.jobRegistry = jobRegistry;
        this.jobOperator = jobOperator;
    }

    private Job getJob(String name)
    {
        try {
            return jobRegistry.getJob(name);
        }
        catch(NoSuchJobException _)
        {
            return null;
        }
    }


    public void executeJob(String jobName)
            throws JobInstanceAlreadyCompleteException, NoSuchJobException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Job foundJob = getJob(jobName);

        if (foundJob != null)
        {
            jobOperator.start(foundJob, EMPTY_PARAMETERS);
        }
    }



}
