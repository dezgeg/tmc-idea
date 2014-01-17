package fi.iki.dezgeg.tmc.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;

public class DoStuffAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        final ModuleManager moduleManager = ModuleManager.getInstance(project);

        File testsDir = new File("/home/tmtynkky/Kotlin/k2014-tira-paja");
        for (final File exerciseDir : testsDir.listFiles()) {
            final String projectFile = exerciseDir.getAbsolutePath() + File.separator + exerciseDir.getName() + ".iml";
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    Module module = moduleManager.newModule(projectFile, "JAVA_MODULE");
                    ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
                    ModifiableRootModel modifiableModel = rootManager.getModifiableModel();

                    String exerciseDirUrl = "file://" + exerciseDir.getAbsolutePath();
                    ContentEntry contentEntry = modifiableModel.addContentEntry(exerciseDirUrl);
                    contentEntry.addSourceFolder(exerciseDirUrl + "/src", false);
                    contentEntry.addSourceFolder(exerciseDirUrl + "/test", true);
                    modifiableModel.commit();
                }
            });

        }

        Module[] modules = moduleManager.getModules();
        ArrayList<String> names = new ArrayList<String>();
        for (Module module : modules) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            String[] contentRootUrls = moduleRootManager.getContentRootUrls();
            String[] sourceRootUrls = moduleRootManager.getSourceRootUrls();

            names.add(module.getName() + "(" +
                    module.getModuleFilePath() + "," +
                    " { " + StringUtils.join(contentRootUrls, ", ") + " }, " +
                    " { " + StringUtils.join(sourceRootUrls, ", ") + " }, " +
                    ")");
        }

        Messages.showMessageDialog(project, StringUtils.join(names, ", "), "TMC-IDEA", Messages.getInformationIcon());
    }
}
