package com.jobscheduler;

/**
 * Configuration settings for the JobScheduler.
 * Uses the Builder Pattern for cleaner code.
 */
public class JobSchedulerConfig {
    private final int poolSize;
    private final int queueCapacity;
    private final int maxRetries;
    private final long baseRetryDelayMs;

    private JobSchedulerConfig(Builder builder) {
        this.poolSize = builder.poolSize;
        this.queueCapacity = builder.queueCapacity;
        this.maxRetries = builder.maxRetries;
        this.baseRetryDelayMs = builder.baseRetryDelayMs;
    }

    // Getters
    public int getPoolSize() { return poolSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public int getMaxRetries() { return maxRetries; }
    public long getBaseRetryDelayMs() { return baseRetryDelayMs; }

    // Builder Class
    public static class Builder {
        private int poolSize = 10;          // Default: 10 threads
        private int queueCapacity = 100;    // Default: 100 jobs buffer
        private int maxRetries = 3;         // Default: 3 retries
        private long baseRetryDelayMs = 50; // Default: 50ms wait

        public Builder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder queueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder baseRetryDelayMs(long baseRetryDelayMs) {
            this.baseRetryDelayMs = baseRetryDelayMs;
            return this;
        }

        public JobSchedulerConfig build() {
            return new JobSchedulerConfig(this);
        }
    }
}
