package com.jobscheduler;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class JobSchedulerServiceTest {

    @Test
    void shouldExecuteJobs() throws Exception {
        JobSchedulerConfig config = new JobSchedulerConfig.Builder()
                .poolSize(2)
                .queueCapacity(10)
                .build();
        JobScheduler scheduler = new JobSchedulerService(config);
        
        Future<String> future = scheduler.submitJob(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        });

        assertEquals("Success", future.get(1, TimeUnit.SECONDS));
        scheduler.shutdown();
    }

    @Test
    void shouldHandleBackpressure() throws Exception {
        JobSchedulerConfig config = new JobSchedulerConfig.Builder()
                .poolSize(1)
                .queueCapacity(1)
                .build();
        JobSchedulerService scheduler = new JobSchedulerService(config);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger completedTasks = new AtomicInteger(0);

        // 1. Fill the pool
        scheduler.submitJob(() -> {
            try { latch.await(); } catch (InterruptedException e) {}
            completedTasks.incrementAndGet();
        });

        // 2. Fill the queue
        scheduler.submitJob(() -> {
            completedTasks.incrementAndGet();
        });

        // 3. Trigger backpressure
        String currentThreadName = Thread.currentThread().getName();
        final String[] taskThreadName = new String[1];

        scheduler.submitJob(() -> {
            taskThreadName[0] = Thread.currentThread().getName();
            completedTasks.incrementAndGet();
        });

        assertEquals(currentThreadName, taskThreadName[0]);
        assertEquals(1, completedTasks.get());

        latch.countDown();
        scheduler.shutdown();
    }

    @Test
    void shouldRetryFailedJobs() throws Exception {
        JobSchedulerConfig config = new JobSchedulerConfig.Builder()
                .poolSize(1)
                .maxRetries(3)
                .baseRetryDelayMs(10)
                .build();
        JobSchedulerService scheduler = new JobSchedulerService(config);
        AtomicInteger attempts = new AtomicInteger(0);

        Future<String> future = scheduler.submitJob(() -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt <= 2) {
                throw new RuntimeException("Fail!"); 
            }
        });

        assertEquals("Success", future.get(1, TimeUnit.SECONDS));
        assertEquals(3, attempts.get());
        
        scheduler.shutdown();
    }

    @Test
    void shouldTimeoutSlowJobs() throws Exception {
        // Setup: Timeout after 100ms, 2 retries
        JobSchedulerConfig config = new JobSchedulerConfig.Builder()
                .poolSize(1)
                .timeoutMs(100)
                .maxRetries(2)
                .baseRetryDelayMs(10)
                .build();
        JobSchedulerService scheduler = new JobSchedulerService(config);
        AtomicInteger attempts = new AtomicInteger(0);

        // This job takes 500ms, but timeout is 100ms
        Future<String> future = scheduler.submitJob(() -> {
            attempts.incrementAndGet();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Thread was killed by timeout
                return;
            }
        });

        // The job should fail after all retries
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            future.get(2, TimeUnit.SECONDS);
        });

        assertTrue(exception.getMessage().contains("Task timed out"), "Should be a timeout error");
        assertEquals(3, attempts.get(), "Should have tried 3 times (1 initial + 2 retries)");

        scheduler.shutdown();
    }
}
