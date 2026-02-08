package com.rebuild.backend.batch;


import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.stereotype.Component;

@Component
public class BatchJobRegisterer {

    private final JobRegistry jobRegistry;

    public BatchJobRegisterer(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }


    public void registerJob(Job createdJob)
    {
        try{
            jobRegistry.register(createdJob);
        }
        catch (DuplicateJobException _){
        }
    }

    public Job getJob(String name)
    {
        try {
            return jobRegistry.getJob(name);
        }
        catch(NoSuchJobException _)
        {
            return null;
        }
    }


}
