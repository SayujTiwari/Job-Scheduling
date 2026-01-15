package com.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class JobSchedulerService implements JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);
    private final ThreadPoolExecutor executor;
    
    // Config for retries
    private final int maxRetries;
    private final long baseRetryDelayMs;

    public JobSchedulerService(int poolSize, int queueCapacity) {
        // Default retry settings: 3 tries, starting at 50ms wait
        this(poolSize, queueCapacity, 3, 50);
    }

    public JobSchedulerService(int poolSize, int queueCapacity, int maxRetries, long baseRetryDelayMs) {
        this.maxRetries = maxRetries;
        this.baseRetryDelayMs = baseRetryDelayMs;

        // Use a fixed pool to reuse threads (faster than creating new ones)
        // ArrayBlockingQueue saves memory compared to a linked list
        this.executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity)
        );

        // If the queue is full, make the submitting thread do the work.
        // This naturally slows down the sender so we don't crash.
        this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public Future<String> submitJob(Runnable job) {
        // Wrap the user's job in our retry logic before sending it to the pool
        RetryableTask wrappedJob = new RetryableTask(job, maxRetries, baseRetryDelayMs);
        return executor.submit(wrappedJob, "Success");
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
