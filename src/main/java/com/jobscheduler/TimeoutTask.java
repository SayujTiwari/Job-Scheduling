package com.jobscheduler;

import java.util.concurrent.*;

/**
 * Wraps a job to ensure it doesn't run longer than a specified timeout.
 */
public class TimeoutTask implements Runnable {
    private final Runnable task;
    private final long timeoutMs;

    public TimeoutTask(Runnable task, long timeoutMs) {
        this.task = task;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void run() {
        if (timeoutMs <= 0) {
            task.run(); // No timeout set
            return;
        }

        ExecutorService tempExecutor = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = tempExecutor.submit(task);
            try {
                // Wait for the task to finish within the timeout
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true); // Attempt to kill the task
                throw new RuntimeException("Task timed out after " + timeoutMs + " ms");
            } catch (ExecutionException e) {
                // Task failed with an exception, rethrow it
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                throw new RuntimeException(e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task execution interrupted", e);
            }
        } finally {
            tempExecutor.shutdownNow();
        }
    }
}
