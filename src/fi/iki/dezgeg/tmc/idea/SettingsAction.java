package fi.iki.dezgeg.tmc.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import fi.iki.dezgeg.tmc.idea.gui.CourseWizard;

public class SettingsAction extends AnAction {
    private static Logger LOG = Logger.getInstance(SettingsAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        new CourseWizard(project).show();
    }
}
