package com.jobscheduler;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class JobSchedulerServiceTest {

    @Test
    void shouldExecuteJobs() throws Exception {
        JobScheduler scheduler = new JobSchedulerService(2, 10);
        
        Future<String> future = scheduler.submitJob(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        });

        assertEquals("Success", future.get(1, TimeUnit.SECONDS));
        scheduler.shutdown();
    }

    @Test
    void shouldHandleBackpressure() throws Exception {
        // Tiny pool and queue to force backpressure quickly
        int poolSize = 1;
        int queueCapacity = 1;
        JobSchedulerService scheduler = new JobSchedulerService(poolSize, queueCapacity);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger completedTasks = new AtomicInteger(0);

        // 1. Fill the pool (1 thread busy)
        scheduler.submitJob(() -> {
            try { latch.await(); } catch (InterruptedException e) {}
            completedTasks.incrementAndGet();
        });

        // 2. Fill the queue (1 slot taken)
        scheduler.submitJob(() -> {
            completedTasks.incrementAndGet();
        });

        // 3. This 3rd task should trigger CallerRunsPolicy (executes in THIS thread)
        String currentThreadName = Thread.currentThread().getName();
        final String[] taskThreadName = new String[1];

        scheduler.submitJob(() -> {
            taskThreadName[0] = Thread.currentThread().getName();
            completedTasks.incrementAndGet();
        });

        // If CallerRunsPolicy worked, the task ran in the main test thread
        assertEquals(currentThreadName, taskThreadName[0], "Task should run in caller thread");
        assertEquals(1, completedTasks.get(), "Only the caller task should be done so far");

        latch.countDown(); // Release the rest
        scheduler.shutdown();
    }
}
