package fi.iki.dezgeg.tmc.idea;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.apache.commons.lang.StringUtils;

import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DoStuffAction extends AnAction {
    Logger LOG = Logger.getInstance(DoStuffAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final RunManager runManager = RunManager.getInstance(project);

        ConfigurationType junitType = null;
        ConfigurationType appType = null;
        for (ConfigurationType type : runManager.getConfigurationFactories()) {
            if (type.getId().equals("JUnit"))
                junitType = type;
            else if (type.getId().equals("Application"))
                appType = type;
        }
        assert junitType != null && appType != null;
        assert junitType.getConfigurationFactories().length == 1;
        assert appType.getConfigurationFactories().length == 1;

        File testsDir = new File("/home/tmtynkky/Kotlin/k2014-tira-paja");
        for (final File exerciseDir : testsDir.listFiles()) {
            processExercise(moduleManager, runManager, junitType.getConfigurationFactories()[0], exerciseDir);
        }

        Module[] modules = moduleManager.getModules();
        ArrayList<String> names = new ArrayList<String>();
        for (Module module : modules) {
            ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            String[] contentRootUrls = moduleRootManager.getContentRootUrls();
            String[] sourceRootUrls = moduleRootManager.getSourceRootUrls();
            Sdk sdk = moduleRootManager.getSdk();

            String s = module.getName() + "(" +
                    module.getModuleFilePath() + "," +
                    " { " + StringUtils.join(contentRootUrls, ", ") + " }, " +
                    " { " + StringUtils.join(sourceRootUrls, ", ") + " }, " +
                    (sdk != null ? sdk.toString() : "<no sdk>") + ", " + ")";
            names.add(s);
            LOG.warn(s);
        }

//        Messages.showMessageDialog(project, StringUtils.join(names, ", "), "TMC-IDEA", Messages.getInformationIcon());


        List<RunnerAndConfigurationSettings> configurations = runManager.getConfigurationSettingsList(junitType);
        for (RunnerAndConfigurationSettings config : configurations) {
            Element element = new Element("foo");
            try {
                config.getConfiguration().writeExternal(element);
            } catch (WriteExternalException e1) {
                e1.printStackTrace();
            }
            LOG.warn("Config: " + config.getType() + " -> " + element);
        }

    }

    private void processExercise(final ModuleManager moduleManager, final RunManager runManager, final ConfigurationFactory junitFactory, final File exerciseDir) {
        final String projectFile = exerciseDir.getAbsolutePath() + File.separator + exerciseDir.getName() + ".iml";
        final String exerciseName = exerciseDir.getName();

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
                modifiableModel.inheritSdk();
                modifiableModel.commit();

                RunnerAndConfigurationSettings runConfiguration = runManager.createRunConfiguration("Test " + exerciseName, junitFactory);
                Element e = new Element("configuration");
                setOption(e, "module", "name", exerciseName);
                setOption(e, "option", "TEST_OBJECT", "package");

                Element scopeOption = new Element("option");
                scopeOption.setAttribute("name", "TEST_SEARCH_SCOPE");
                Element scopeValue = new Element("value");
                scopeValue.setAttribute("defaultName", "singleModule");
                scopeOption.addContent(scopeValue);
                e.addContent(scopeOption);

                try {
                    runConfiguration.getConfiguration().readExternal(e);
                } catch (InvalidDataException e1) {
                    e1.printStackTrace();
                }

                runManager.addConfiguration(runConfiguration, false);
            }
        });
    }

    private void setOption(Element e, String tag, String key, String value) {
        Element child = new Element(tag);
        child.setAttribute(key, value);
        e.addContent(child);
    }
}
