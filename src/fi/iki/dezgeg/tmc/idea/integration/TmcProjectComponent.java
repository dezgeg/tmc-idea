package fi.iki.dezgeg.tmc.idea.integration;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import fi.helsinki.cs.tmc.core.Core;
import org.jetbrains.annotations.NotNull;

public class TmcProjectComponent implements ProjectComponent {
    private Project project;

    public TmcProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "TMC.ProjectComponent";
    }

    public void projectOpened() {
        System.out.println("Hello world from TmcProjectComponent: " + project);
        Core.setErrorHandler(new IdeaTmcErrorHandler());
        Core.setTaskRunner(new IdeaTmcTaskRunner(project));
    }

    public void projectClosed() {
        // called when project is being closed
    }
}
