package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import fi.helsinki.cs.tmc.core.Core;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class ExerciseSelectorDialog extends DialogWrapper {
    private JPanel contentPane;
    private JList<String> exerciseListBox;

    private List<Exercise> exercises;

    public ExerciseSelectorDialog(List<Exercise> downloadableExercises, Project project) {
        super(project);
        this.exercises = downloadableExercises;

        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (Exercise exercise : exercises) {
            listModel.addElement(exercise.getName());
        }
        exerciseListBox.setModel(listModel);
        exerciseListBox.setSelectionInterval(0, exercises.size() - 1);

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public static void showDialog(Project project) {
        Course currentCourse = Core.getCourseDAO().getCurrentCourse(Core.getSettings());
        Core.getUpdater().updateExercises(currentCourse);
        List<Exercise> downloadableExercises = currentCourse.getDownloadableExercises();

        if (downloadableExercises.isEmpty())
            return;

        new ExerciseSelectorDialog(downloadableExercises, project).show();
    }
}
