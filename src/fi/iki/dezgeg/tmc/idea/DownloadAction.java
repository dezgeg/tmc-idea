package fi.iki.dezgeg.tmc.idea;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import fi.iki.dezgeg.tmc.api.Course;
import fi.iki.dezgeg.tmc.api.Exercise;
import fi.iki.dezgeg.tmc.api.TmcApi;
import org.jdom.Element;

import java.io.File;
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
                Course course = tmcApi.getCourses().get("k2014-tira-paja");

                Map<String,Exercise> exercises = tmcApi.getExercises(course);
                int i = 0;
                for (Exercise exercise : exercises.values()) {
                    progressIndicator.setText2("Downloading exercise " + exercise.getName());
                    progressIndicator.setFraction((double)i / exercises.size());
                    tmcApi.downloadExercise(exercise, "/tmp/foo/");
                    i++;
                }
            }
        };
        progressManager.runProcessWithProgressSynchronously(runnable, "Downloading exercises...", false, project);

    }
}
