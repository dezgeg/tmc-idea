package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.project.Project;
import fi.helsinki.cs.tmc.core.Core;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.services.ProjectOpener;
import fi.iki.dezgeg.tmc.idea.integration.NetbeansProjectImporter;

import java.io.File;

public class IdeaProjectOpener implements ProjectOpener {
    private Project project;

    public IdeaProjectOpener(Project project) {
        this.project = project;
    }

    @Override
    public void open(Exercise exercise) {
        new NetbeansProjectImporter(project, new File(exercise.getProject().getRootPath())).importProject();
        // XXX TODO - not here. Works around an arguably plugin-core bug
        // If a project is redownloaded, then ProjectDAO is not saved (see DownloaderTask)
        Core.getProjectDAO().save();
    }
}
