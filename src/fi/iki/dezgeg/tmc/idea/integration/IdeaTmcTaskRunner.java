package fi.iki.dezgeg.tmc.idea.integration;

import fi.helsinki.cs.tmc.core.async.BackgroundTask;
import fi.helsinki.cs.tmc.core.async.BackgroundTaskListener;
import fi.helsinki.cs.tmc.core.async.BackgroundTaskRunner;

public class IdeaTmcTaskRunner implements BackgroundTaskRunner {
    @Override
    public void runTask(BackgroundTask backgroundTask) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void runTask(BackgroundTask backgroundTask, BackgroundTaskListener backgroundTaskListener) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void cancelTask(BackgroundTask backgroundTask) {
        throw new RuntimeException("Not implemented!");
    }
}
