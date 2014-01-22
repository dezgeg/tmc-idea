package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import fi.iki.dezgeg.tmc.api.Course;
import fi.iki.dezgeg.tmc.api.TmcApi;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CourseWizard extends AbstractWizard<CourseWizardStep> {
    protected TmcApi tmcApi;
    protected Map<String, Course> courseList;

    public CourseWizard(@Nullable Project project) {
        super("Start new TMC course", project);
        tmcApi = new TmcApi();
        addStep(new AccountConfigStep(this));
        addStep(new CourseSelectionStep(this));
        init();
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
}
