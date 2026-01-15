package com.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class JobSchedulerService implements JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);
    private final ThreadPoolExecutor executor;
    private final JobSchedulerConfig config;

    public JobSchedulerService(JobSchedulerConfig config) {
        this.config = config;

        // Use config values for the pool and queue
        this.executor = new ThreadPoolExecutor(
                config.getPoolSize(),
                config.getPoolSize(),
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(config.getQueueCapacity())
        );

        // Slow down the sender if the queue is full
        this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public Future<String> submitJob(Runnable job) {
        // Use config values for the retry wrapper
        RetryableTask wrappedJob = new RetryableTask(
                job, 
                config.getMaxRetries(), 
                config.getBaseRetryDelayMs()
        );
        return executor.submit(wrappedJob, "Success");
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
