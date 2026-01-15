package com.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class JobSchedulerService implements JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);
    private final ThreadPoolExecutor executor;

    public JobSchedulerService(int poolSize, int queueCapacity) {
        // 1. High-Throughput: ThreadPoolExecutor reuses threads to avoid creation overhead.
        //    We set corePoolSize == maximumPoolSize to keep a fixed worker set active.
        // 2. Backpressure: ArrayBlockingQueue is bounded and has less memory overhead (no node objects)
        //    than LinkedBlockingQueue, which is better for high-throughput/low-GC.
        this.executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity)
        );

        // 3. Backpressure (Congestion Control):
        //    If the queue is full, CallerRunsPolicy makes the *submitting* thread execute the task.
        //    This effectively slows down the producer ("throttling") prevents memory overflow.
        this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
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
