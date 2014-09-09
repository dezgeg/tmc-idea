package fi.iki.dezgeg.tmc.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import fi.helsinki.cs.tmc.core.Core;
import fi.iki.dezgeg.tmc.idea.gui.IdeaProjectOpener;

public class ReopenExercisesAction extends AnAction {
    private static Logger LOG = Logger.getInstance(ReopenExercisesAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        IdeaProjectOpener opener = new IdeaProjectOpener(e.getData(PlatformDataKeys.PROJECT));

        for (fi.helsinki.cs.tmc.core.domain.Project tmcProject : Core.getProjectDAO().getProjects()) {
            opener.open(tmcProject.getExercise());
        }
    }
}
