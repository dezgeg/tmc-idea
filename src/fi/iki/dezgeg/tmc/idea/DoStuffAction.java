package fi.iki.dezgeg.tmc.idea;

import com.intellij.execution.RunManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import fi.iki.dezgeg.tmc.idea.integration.NetbeansProjectImporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class DoStuffAction extends AnAction {
    Logger LOG = Logger.getInstance(DoStuffAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final RunManager runManager = RunManager.getInstance(project);

        final File testsDir = new File("/tmp/bar");

        Task.Backgroundable task = new Task.Backgroundable(project, "Importing exercises...", false) {
            @Override
            public void run(@NotNull final ProgressIndicator progressIndicator) {
                int i = 0;
                final ArrayList<File> files = new ArrayList<File>(FileUtils.listFiles(testsDir,
                        new NameFileFilter("build.xml"), DirectoryFileFilter.DIRECTORY));
                Collections.sort(files);
                for (File f : files) {
                    final File exerciseDir = f.getParentFile();

                    new NetbeansProjectImporter(project, exerciseDir).importProject();
                    i++;
                }
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task,
                new BackgroundableProcessIndicator(task));
    }
}
