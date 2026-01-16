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
    private final long timeoutMs;

    private JobSchedulerConfig(Builder builder) {
        this.poolSize = builder.poolSize;
        this.queueCapacity = builder.queueCapacity;
        this.maxRetries = builder.maxRetries;
        this.baseRetryDelayMs = builder.baseRetryDelayMs;
        this.timeoutMs = builder.timeoutMs;
    }

    // Getters
    public int getPoolSize() { return poolSize; }
    public int getQueueCapacity() { return queueCapacity; }
    public int getMaxRetries() { return maxRetries; }
    public long getBaseRetryDelayMs() { return baseRetryDelayMs; }
    public long getTimeoutMs() { return timeoutMs; }

    // Builder Class
    public static class Builder {
        private int poolSize = 10;
        private int queueCapacity = 100;
        private int maxRetries = 3;
        private long baseRetryDelayMs = 50;
        private long timeoutMs = 0; // Default: No timeout

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

        public Builder timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public JobSchedulerConfig build() {
            return new JobSchedulerConfig(this);
        }
    }
}
