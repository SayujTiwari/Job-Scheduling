package com.jobscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A demo application that simulates high load.
 * It submits thousands of jobs with different behaviors:
 * - Fast/Successful jobs
 * - Flaky jobs (randomly fail)
 * - Slow jobs (trigger timeouts)
 */
public class LoadTest {
    private static final Logger logger = LoggerFactory.getLogger(LoadTest.class);
    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        // 1. Configure the engine
        JobSchedulerConfig config = new JobSchedulerConfig.Builder()
                .poolSize(10)          // 10 worker threads
                .queueCapacity(100)    // Buffer for 100 jobs
                .maxRetries(3)         // 3 retries for failures
                .baseRetryDelayMs(50)  // Start retries at 50ms
                .timeoutMs(200)        // Kill jobs taking > 200ms
                .build();

        JobSchedulerService scheduler = new JobSchedulerService(config);

        logger.info("Starting Load Test: Submitting 1000 jobs...");

        // 2. Submit 1000 varied jobs
        for (int i = 0; i < 1000; i++) {
            final int jobId = i;
            scheduler.submitJob(() -> simulateWork(jobId));
            
            // Small sleep to not overwhelm the console immediately
            if (i % 100 == 0) Thread.sleep(10); 
        }

        // 3. Wait for jobs to process
        logger.info("All jobs submitted. Waiting for completion...");
        Thread.sleep(5000); 

        // 4. Print Results
        MetricsRegistry metrics = MetricsRegistry.getInstance();
        logger.info("-------------------------------------------");
        logger.info("LOAD TEST RESULTS:");
        logger.info("Completed Jobs: {}", metrics.getCompletedJobs());
        logger.info("Failed Jobs:    {}", metrics.getFailedJobs());
        logger.info("Total Retries:  {}", metrics.getTotalRetries());
        logger.info("-------------------------------------------");

        scheduler.shutdown();
    }

    private static void simulateWork(int id) {
        int choice = random.nextInt(10);

        if (choice < 7) {
            // 70% Success: Very fast
            return;
        } else if (choice < 9) {
            // 20% Flaky: Throw random error
            throw new RuntimeException("Random Failure in Job " + id);
        } else {
            // 10% Slow: Trigger timeout
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Interrupted by timeout guard
            }
        }
    }
}
