package fi.iki.dezgeg.tmc.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import fi.iki.dezgeg.tmc.api.Course;
import fi.iki.dezgeg.tmc.api.Exercise;
import fi.iki.dezgeg.tmc.api.TmcApi;

import java.util.Map;

public class DownloadAction extends AnAction {
    Logger LOG = Logger.getInstance(DownloadAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        final TmcApi tmcApi = new TmcApi();
        tmcApi.setCredentials(TmcApi.DEFAULT_SERVER_URL, "dezgeg", "dezgeg");

        final ProgressManager progressManager = ProgressManager.getInstance();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ProgressIndicator progressIndicator = progressManager.getProgressIndicator();
                progressIndicator.setText2("Downloading course information");
                Course course = tmcApi.getCourses().get("s2014-tira");

                Map<String, Exercise> exercises = tmcApi.getExercises(course);
                int i = 0;
                for (Exercise exercise : exercises.values()) {
                    progressIndicator.setText2("Downloading exercise " + exercise.getName());
                    progressIndicator.setFraction((double) i / exercises.size());
                    tmcApi.downloadExercise(exercise, "/tmp/foo/");
                    i++;
                }
            }
        };
        progressManager.runProcessWithProgressSynchronously(runnable, "Downloading exercises...", false, project);

    }
}
