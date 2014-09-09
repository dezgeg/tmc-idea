package fi.iki.dezgeg.tmc.idea.integration;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import fi.helsinki.cs.tmc.core.async.BackgroundTask;
import fi.helsinki.cs.tmc.core.async.BackgroundTaskListener;
import fi.helsinki.cs.tmc.core.async.BackgroundTaskRunner;
import fi.helsinki.cs.tmc.core.async.TaskStatusMonitor;
import org.jetbrains.annotations.NotNull;

public class IdeaTmcTaskRunner implements BackgroundTaskRunner {
    private Project project;

    public IdeaTmcTaskRunner(Project project) {
        this.project = project;
    }

    @Override
    public void runTask(BackgroundTask backgroundTask) {
        runTask(backgroundTask, null);
    }

    @Override
    public void runTask(final BackgroundTask backgroundTask, final BackgroundTaskListener backgroundTaskListener) {
        Task.Backgroundable ideaTask = new Task.Backgroundable(project, backgroundTask.getDescription(), true) {
            @Override
            public void run(@NotNull final ProgressIndicator progressIndicator) {
                if (backgroundTaskListener != null)
                    backgroundTaskListener.onBegin();

                int ret = backgroundTask.start(new TaskStatusMonitor() {
                    public int curSteps;
                    public int maxSteps;

                    @Override
                    public void startProgress(String status, int maxSteps) {
                        progressIndicator.setText2(status);
                        this.maxSteps = maxSteps;
                    }

                    @Override
                    public void incrementProgress(int increment) {
                        curSteps += increment;
                        progressIndicator.setFraction((double) curSteps / maxSteps);
                    }

                    @Override
                    public boolean isCancelRequested() {
                        // TODO!
                        return false;
                    }
                });

                if (backgroundTaskListener == null)
                    return;
                switch (ret) {
                    case BackgroundTask.RETURN_FAILURE:
                        backgroundTaskListener.onFailure();
                        break;
                    case BackgroundTask.RETURN_INTERRUPTED:
                        backgroundTaskListener.onInterruption();
                        break;
                    case BackgroundTask.RETURN_SUCCESS:
                        backgroundTaskListener.onSuccess();
                        break;
                }
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(ideaTask,
                new BackgroundableProcessIndicator(ideaTask));
    }

    @Override
    public void cancelTask(BackgroundTask backgroundTask) {
        throw new RuntimeException("Not implemented!");
    }
}
