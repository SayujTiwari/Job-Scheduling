package com.jobscheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    @BeforeEach
    void setup() {
        MetricsRegistry.getInstance().reset();
    }

    @Test
    void shouldUpdateMetricsOnExecution() throws Exception {
        JobSchedulerConfig config = new JobSchedulerConfig.Builder()
                .poolSize(1)
                .maxRetries(1)
                .baseRetryDelayMs(10)
                .build();
        JobSchedulerService scheduler = new JobSchedulerService(config);

        // 1. Submit a successful job
        scheduler.submitJob(() -> {}).get();

        // 2. Submit a failing job (fails once, then succeeds)
        // logic: fail on attempt 1, succeed on attempt 2
        AtomicInteger attempts = new AtomicInteger(0);
        scheduler.submitJob(() -> {
            if (attempts.incrementAndGet() == 1) {
                throw new RuntimeException("Fail once");
            }
        }).get();

        // 3. Submit a totally failing job (fails > maxRetries)
        Future<String> failFuture = scheduler.submitJob(() -> {
            throw new RuntimeException("Always fail");
        });
        
        assertThrows(ExecutionException.class, () -> failFuture.get());

        // Assert Metrics
        assertEquals(2, MetricsRegistry.getInstance().getCompletedJobs(), "Should have 2 successes");
        assertEquals(1, MetricsRegistry.getInstance().getFailedJobs(), "Should have 1 final failure");
        assertEquals(2, MetricsRegistry.getInstance().getTotalRetries(), "1 retry for job #2 + 1 retry for job #3 before it died");
        
        scheduler.shutdown();
    }
}
