package com.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class JobSchedulerService implements JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);
    private final ThreadPoolExecutor executor;

    public JobSchedulerService(int poolSize, int queueCapacity) {
        // 1. Manually construct ThreadPoolExecutor
        // 2. Bounded Queue: LinkedBlockingQueue with fixed capacity prevents Heap Exhaustion
        this.executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity)
        );
    }

    @Override
    public Future<String> submitJob(Runnable job) {
        return executor.submit(job, "Success");
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
