package fi.iki.dezgeg.tmc.idea;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class DoStuffAction extends AnAction {
    Logger LOG = Logger.getInstance(DoStuffAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
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

        final File testsDir = new File("/tmp/foo");

        final ConfigurationType finalJunitType = junitType;
        final ConfigurationType finalAppType = appType;

        Task.Backgroundable task = new Task.Backgroundable(project, "Importing exercises...", false) {
            @Override
            public void run(@NotNull final ProgressIndicator progressIndicator) {
                int i = 0;
                final ArrayList<File> files = new ArrayList<File>(FileUtils.listFiles(testsDir, new NameFileFilter("build.xml"), DirectoryFileFilter.DIRECTORY));
                Collections.sort(files);
                for (File f : files) {
                    final File exerciseDir = f.getParentFile();

                    final int finalI = i;
                    final Module[] outModule = new Module[1];
                    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            progressIndicator.setText2("Importing exercise " + exerciseDir.getName());
                            progressIndicator.setFraction((double) finalI / files.size());

                            outModule[0] = processExercise(project, finalJunitType.getConfigurationFactories()[0],
                                    exerciseDir);
                        }
                    }, ModalityState.NON_MODAL);

                    createRunMainConfiguration(outModule[0], project, runManager, exerciseDir.getName(), finalAppType.getConfigurationFactories()[0]);
                    i++;
                }
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    private Module processExercise(final Project project, final ConfigurationFactory junitFactory, final File exerciseDir) {
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final RunManager runManager = RunManager.getInstance(project);
        final String projectFile = exerciseDir.getAbsolutePath() + File.separator + exerciseDir.getName() + ".iml";
        final String exerciseName = exerciseDir.getName();

        final Module[] retModule = new Module[1];
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
                // RunMainConfiguration not done here since we are in GUI thread.

                retModule[0] = module;
            }
        });
        return retModule[0];
    }

    private void createRunMainConfiguration(final Module module, final Project project, final RunManager runManager, final String exerciseName, final ConfigurationFactory appFactory) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().
                        runReadAction(new Runnable() {
                                          @Override
                                          public void run() {
                                              GlobalSearchScope moduleScope = module.getModuleScope(false);
                                              Query<PsiClass> search = AllClassesSearch.search(moduleScope, project);
                                              search.forEach(new Processor<PsiClass>() {
                                                  @Override
                                                  public boolean process(PsiClass psiClass) {
                                                      if (PsiMethodUtil.hasMainMethod(psiClass)) {
                                                          createRunMainConfigurationForClass(runManager, exerciseName, appFactory, psiClass);
                                                          return false;
                                                      }
                                                      return true;
                                                  }
                                              });
                                          }
                                      }
                        );
            }
        };
        DumbService.getInstance(project).smartInvokeLater(runnable);
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

        // XXX: we are inside read lock but this succeeds?
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
