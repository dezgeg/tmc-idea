package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.openapi.project.Project;
import fi.iki.dezgeg.tmc.api.Course;
import fi.iki.dezgeg.tmc.api.TmcApi;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Map;

public class CourseWizard extends AbstractWizard<CourseWizardStep> {
    protected TmcApi tmcApi;
    protected Map<String, Course> courseList;
    protected DocumentListener updateButtonsListener = new UpdateButtonsListener();
    protected Project project;

    public CourseWizard(@Nullable Project project) {
        super("Start new TMC course", project);
        this.project = project;

        tmcApi = new TmcApi();
        addStep(new AccountConfigStep(this));
        addStep(new CourseSelectionStep(this));
        init();
    }

    @Override
    protected boolean canGoNext() {
        if (!getCurrentStepObject().canGoNext())
            return false;
        return super.canGoNext();
    }

    @Override
    protected void doNextAction() {
        if (!getCurrentStepObject().validate())
            return;
        super.doNextAction();
    }

    @Nullable
    @Override
    protected String getHelpID() {
        return null;
    }

    private class UpdateButtonsListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            updateButtons();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            updateButtons();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            updateButtons();
        }
    }
}
