package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.ide.wizard.StepAdapter;
import fi.iki.dezgeg.tmc.api.Course;
import fi.iki.dezgeg.tmc.api.TmcApi;

import java.util.Map;

public class CourseWizardStep extends StepAdapter {
    protected CourseWizard wizard;

    public CourseWizardStep(CourseWizard wizard) {
        this.wizard = wizard;
    }

    public boolean validate() {
        return true;
    }

    public boolean canGoNext() {
        return true;
    }
}
