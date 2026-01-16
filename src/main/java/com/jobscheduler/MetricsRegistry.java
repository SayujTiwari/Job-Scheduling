package com.jobscheduler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A Singleton registry to track global statistics for the Job Scheduler.
 * Uses AtomicLongs for thread-safe counting.
 */
public class MetricsRegistry {
    private static final MetricsRegistry INSTANCE = new MetricsRegistry();

    private final AtomicLong completedJobs = new AtomicLong(0);
    private final AtomicLong failedJobs = new AtomicLong(0);
    private final AtomicLong totalRetries = new AtomicLong(0);

    // Private constructor for Singleton pattern
    private MetricsRegistry() {}

    public static MetricsRegistry getInstance() {
        return INSTANCE;
    }

    public void incrementCompletedJobs() {
        completedJobs.incrementAndGet();
    }

    public void incrementFailedJobs() {
        failedJobs.incrementAndGet();
    }

    public void incrementRetries() {
        totalRetries.incrementAndGet();
    }

    public long getCompletedJobs() {
        return completedJobs.get();
    }

    public long getFailedJobs() {
        return failedJobs.get();
    }

    public long getTotalRetries() {
        return totalRetries.get();
    }

    // Useful for testing to reset state
    public void reset() {
        completedJobs.set(0);
        failedJobs.set(0);
        totalRetries.set(0);
    }
}
