package com.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

// Wraps a job to automatically retry if it fails.
// Uses "Jittered Exponential Backoff" to retry smartly.
public class RetryableTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RetryableTask.class);
    
    private final Runnable task;
    private final int maxRetries;
    private final long baseDelayMs;

    public RetryableTask(Runnable task, int maxRetries, long baseDelayMs) {
        this.task = task;
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
    }

    @Override
    public void run() {
        int attempt = 0;
        while (true) {
            try {
                task.run();
                return; // Worked! We are done.
            } catch (RuntimeException e) {
                attempt++;
                if (attempt > maxRetries) {
                    logger.error("Task failed after {} attempts. Giving up.", attempt, e);
                    throw e; // Retried enough, now we actually fail.
                }

                handleBackoff(attempt);
            }
        }
    }

    private void handleBackoff(int attempt) {
        // Wait time doubles each try: 100ms, 200ms, 400ms...
        long exponentialDelay = baseDelayMs * (1L << (attempt - 1));
        
        // Add random "jitter" so threads don't all wake up at the exact same time
        long jitter = ThreadLocalRandom.current().nextLong(0, exponentialDelay + 1);
        
        long totalWait = exponentialDelay + jitter;

        logger.warn("Task failed (attempt {}). Retrying in {} ms...", attempt, totalWait);
        
        try {
            Thread.sleep(totalWait);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); 
            throw new RuntimeException("Retry interrupted", ie);
        }
    }
}
