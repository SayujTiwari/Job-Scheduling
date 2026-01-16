package com.jobscheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetricsRegistryTest {

    @BeforeEach
    void setup() {
        MetricsRegistry.getInstance().reset();
    }

    @Test
    void shouldTrackMetricsCorrectly() {
        MetricsRegistry metrics = MetricsRegistry.getInstance();

        metrics.incrementCompletedJobs();
        metrics.incrementCompletedJobs();
        metrics.incrementFailedJobs();
        metrics.incrementRetries();

        assertEquals(2, metrics.getCompletedJobs());
        assertEquals(1, metrics.getFailedJobs());
        assertEquals(1, metrics.getTotalRetries());
    }
}
