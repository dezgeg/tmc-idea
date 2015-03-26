package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import fi.helsinki.cs.tmc.core.Core;
import fi.helsinki.cs.tmc.core.async.tasks.DownloaderTask;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.services.ProjectDownloader;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseSelectorDialog extends DialogWrapper {
    private JPanel contentPane;
    private JList<String> exerciseListBox;

    private Project project;
    private List<Exercise> exercises;

    public ExerciseSelectorDialog(List<Exercise> downloadableExercises, Project project) {
        super(project);
        this.project = project;
        this.exercises = downloadableExercises;

        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (Exercise exercise : exercises) {
            listModel.addElement(exercise.getName());
        }
        exerciseListBox.setModel(listModel);
        exerciseListBox.setSelectionInterval(0, exercises.size() - 1);

        init();
    }

    public static void showDialog(Project project) {
        Course currentCourse = Core.getCourseDAO().getCurrentCourse(Core.getSettings());
        Core.getUpdater().updateExercises(currentCourse);
        List<Exercise> downloadableExercises = currentCourse.getDownloadableExercises();

        if (downloadableExercises.isEmpty())
            return;

        new ExerciseSelectorDialog(downloadableExercises, project).show();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        List<Exercise> selectedExercises = new ArrayList<Exercise>();
        for (int i : exerciseListBox.getSelectedIndices()) {
            selectedExercises.add(exercises.get(i));
        }

        DownloaderTask task = new DownloaderTask(new ProjectDownloader(Core.getServerManager()),
                new IdeaProjectOpener(project), selectedExercises, Core.getProjectDAO(), Core.getSettings(),
                new IdeaUIInvoker(project), Core.getIOFactory());
        Core.getTaskRunner().runTask(task);
    }
}
