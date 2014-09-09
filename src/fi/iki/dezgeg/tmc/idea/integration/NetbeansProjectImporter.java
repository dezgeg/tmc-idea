package fi.iki.dezgeg.tmc.idea.integration;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
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

public class NetbeansProjectImporter {
    private RunManager runManager;
    private Project project;
    private File exerciseDir;
    private String exerciseName;
    private ConfigurationFactory junitFactory;
    private ConfigurationFactory appFactory;

    public NetbeansProjectImporter(Project project, File exerciseDir) {
        this.project = project;
        this.exerciseDir = exerciseDir;
        exerciseName = exerciseDir.getName();
        runManager = RunManager.getInstance(project);
        initConfigurationFactories();
    }

    private static void setOption(Element e, String tag, String name, String value) {
        Element child = new Element(tag);
        child.setAttribute("name", name);
        if (value != null)
            child.setAttribute("value", value);
        e.addContent(child);
    }

    public void initConfigurationFactories() {
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

        junitFactory = junitType.getConfigurationFactories()[0];
        appFactory = appType.getConfigurationFactories()[0];
    }

    public void createRunMainConfiguration(final Module module) {
        final Runnable readAction = new Runnable() {
            @Override
            public void run() {
                GlobalSearchScope moduleScope = module.getModuleScope(false);
                Query<PsiClass> search = AllClassesSearch.search(moduleScope, project);
                search.forEach(new Processor<PsiClass>() {
                    @Override
                    public boolean process(PsiClass psiClass) {
                        if (PsiMethodUtil.hasMainMethod(psiClass)) {
                            createRunMainConfigurationForClass(exerciseName, psiClass);
                            return false;
                        }
                        return true;
                    }
                });
            }
        };

        DumbService.getInstance(project).smartInvokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runReadAction(readAction);
            }
        });
    }

    public Module createModule() {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final String projectFile = exerciseDir.getAbsolutePath() + File.separator + exerciseName + ".iml";

        final Module[] retModule = new Module[1];
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
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

                        createRunTestsConfiguration();
                        // RunMainConfiguration not done here since we are in GUI thread.

                        retModule[0] = module;
                    }
                });
            }
        }, ModalityState.NON_MODAL);
        return retModule[0];
    }

    private void createRunMainConfigurationForClass(String exerciseName, PsiClass mainClass) {
        RunnerAndConfigurationSettings runTestsConfiguration =
                runManager.createRunConfiguration("Run " + exerciseName, appFactory);
        Element e = new Element("configuration");
        setOption(e, "module", exerciseName, null);
        setOption(e, "option", "MAIN_CLASS_NAME", mainClass.getQualifiedName());

        try {
            runTestsConfiguration.getConfiguration().readExternal(e);
        } catch (InvalidDataException e1) {
            e1.printStackTrace();
        }

        // XXX: we are inside read lock but this succeeds?
        runManager.addConfiguration(runTestsConfiguration, false);
    }

    private void createRunTestsConfiguration() {
        RunnerAndConfigurationSettings runTestsConfiguration =
                runManager.createRunConfiguration("Test " + exerciseName, junitFactory);
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

    public void importProject() {
        Module module = createModule();
        createRunMainConfiguration(module);
    }

}
