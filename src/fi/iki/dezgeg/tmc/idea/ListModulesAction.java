package fi.iki.dezgeg.tmc.idea;

import com.intellij.execution.RunManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class ListModulesAction extends AnAction {
    private static Logger LOG = Logger.getInstance(ListModulesAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final RunManager runManager = RunManager.getInstance(project);

        Module[] modules = moduleManager.getModules();
        ArrayList<String> names = new ArrayList<String>();
        for (Module module : modules) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
            LibraryTable moduleLibraryTable = moduleRootManager.getModifiableModel().getModuleLibraryTable();

            String[] contentRootUrls = moduleRootManager.getContentRootUrls();
            String[] sourceRootUrls = moduleRootManager.getSourceRootUrls();
            Library[] libraries = moduleRootManager.getModifiableModel().getModuleLibraryTable().getLibraries();
            Sdk sdk = moduleRootManager.getSdk();

            String s = module.getName() + "(" +
                    module.getModuleFilePath() + "," +
                    " { " + StringUtils.join(contentRootUrls, ", ") + " }, " +
                    " { " + StringUtils.join(sourceRootUrls, ", ") + " }, " +
                    " { " + StringUtils.join(libraries, ", ") + " }, " +
                    (sdk != null ? sdk.toString() : "<no sdk>") + ", " + ")";
            names.add(s);
            LOG.warn(s);
        }
    }
}
