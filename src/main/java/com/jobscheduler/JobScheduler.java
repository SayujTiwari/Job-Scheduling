package com.jobscheduler;

import java.util.concurrent.Future;

public interface JobScheduler {
    Future<String> submitJob(Runnable job);
    void shutdown();
}
