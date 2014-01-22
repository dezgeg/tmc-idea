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
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import org.jdom.Element;

import java.io.File;

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
            processExercise(project,
                    junitType.getConfigurationFactories()[0], appType.getConfigurationFactories()[0], exerciseDir);
        }

    }

    private void processExercise(final Project project, final ConfigurationFactory junitFactory, final ConfigurationFactory appFactory, final File exerciseDir) {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final RunManager runManager = RunManager.getInstance(project);
        final String projectFile = exerciseDir.getAbsolutePath() + File.separator + exerciseDir.getName() + ".iml";
        final String exerciseName = exerciseDir.getName();

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                Module module = moduleManager.newModule(projectFile, "JAVA_MODULE");
                ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
                ModifiableRootModel modifiableModel = rootManager.getModifiableModel();
                LibraryTable moduleLibraryTable = modifiableModel.getModuleLibraryTable();

                String exerciseDirUrl = "file://" + exerciseDir.getAbsolutePath();
                ContentEntry contentEntry = modifiableModel.addContentEntry(exerciseDirUrl);
                contentEntry.addSourceFolder(exerciseDirUrl + "/src", false);
                contentEntry.addSourceFolder(exerciseDirUrl + "/test", true);
                modifiableModel.inheritSdk();

                Library library = moduleLibraryTable.createLibrary("libs");
                Library.ModifiableModel libraryModifiableModel = library.getModifiableModel();
                libraryModifiableModel.addJarDirectory(exerciseDirUrl + "/lib", true);
                libraryModifiableModel.commit();
                modifiableModel.commit();

                createRunTestsConfiguration(runManager, exerciseName, junitFactory);
                createRunMainConfiguration(module, project, runManager, exerciseName, appFactory);
            }
        });
    }

    private void createRunMainConfiguration(Module module, Project project, final RunManager runManager, final String exerciseName, final ConfigurationFactory appFactory) {
        GlobalSearchScope moduleScope = module.getModuleScope(false);
        Query<PsiClass> search = AllClassesSearch.search(moduleScope, project);
        search.forEach(new Processor<PsiClass>() {
            @Override
            public boolean process(PsiClass psiClass) {
                if (PsiMethodUtil.hasMainMethod(psiClass)) {
                    createRunMainConfigurationForClass(runManager, exerciseName, appFactory, psiClass);
                    LOG.warn(psiClass.toString());
                }
                return true;
            }
        });
    }

    private void createRunMainConfigurationForClass(RunManager runManager, String exerciseName, ConfigurationFactory appFactory, PsiClass mainClass) {
        RunnerAndConfigurationSettings runTestsConfiguration = runManager.createRunConfiguration("Run " + exerciseName, appFactory);
        Element e = new Element("configuration");
        setOption(e, "module", exerciseName, null);
        setOption(e, "option", "MAIN_CLASS_NAME", mainClass.getQualifiedName());

        try {
            runTestsConfiguration.getConfiguration().readExternal(e);
        } catch (InvalidDataException e1) {
            e1.printStackTrace();
        }

        runManager.addConfiguration(runTestsConfiguration, false);
    }

    private void createRunTestsConfiguration(RunManager runManager, String exerciseName, ConfigurationFactory junitFactory) {
        RunnerAndConfigurationSettings runTestsConfiguration = runManager.createRunConfiguration("Test " + exerciseName, junitFactory);
        Element e = new Element("configuration");
        setOption(e, "module", exerciseName, null);
        setOption(e, "option", "TEST_OBJECT", "package");

        Element scopeOption = new Element("option");
        scopeOption.setAttribute("name", "TEST_SEARCH_SCOPE");
        Element scopeValue = new Element("value");
        scopeValue.setAttribute("defaultName", "singleModule");
        scopeOption.addContent(scopeValue);
        e.addContent(scopeOption);

        try {
            runTestsConfiguration.getConfiguration().readExternal(e);
        } catch (InvalidDataException e1) {
            e1.printStackTrace();
        }

        runManager.addConfiguration(runTestsConfiguration, false);
    }

    private void setOption(Element e, String tag, String name, String value) {
        Element child = new Element(tag);
        child.setAttribute("name", name);
        if (value != null)
            child.setAttribute("value", value);
        e.addContent(child);
    }
}
