package fi.iki.dezgeg.tmc.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import fi.helsinki.cs.tmc.core.Core;
import fi.helsinki.cs.tmc.core.async.BackgroundTaskListener;
import fi.helsinki.cs.tmc.core.async.tasks.UploaderTask;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.SubmissionResult;
import fi.helsinki.cs.tmc.core.services.ProjectUploader;
import fi.iki.dezgeg.tmc.idea.gui.IdeaUIInvoker;

public class SubmitAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        FileDocumentManager.getInstance().saveAllDocuments();

        Project ideaProject = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            // FIXME - try harder to determine focused editor
            Messages.showErrorDialog("No editor window focused.", "Cannot Upload");
            return;
        }
        Document document = editor.getDocument();

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        // Module module = ModuleUtil.findModuleForFile(virtualFile, ideaProject);
        String openFilePath = virtualFile.getPath(); // XXX test non-fs files
        ProjectUploader uploader = new ProjectUploader(Core.getServerManager());

        fi.helsinki.cs.tmc.core.domain.Project tmcProject = Core.getProjectDAO().getProjectByFile(openFilePath);
        if (tmcProject == null) {
            Messages.showErrorDialog("Focused file doesn't belong to a TMC project.", "Cannot Upload");
            return;
        }
        if (tmcProject.getExercise().hasDeadlinePassed()) {
            Messages.showErrorDialog("Deadline for this course has been passed.", "Cannot Upload");
            return;
        }

        final IdeaUIInvoker uiInvoker = new IdeaUIInvoker(ideaProject);
        // FIXME[plugin-core]: UploaderTask shouldn't take a file path...
        final UploaderTask task = new UploaderTask(uploader, openFilePath, Core.getProjectDAO(), uiInvoker);
        BackgroundTaskListener taskListener = new BackgroundTaskListener() {
            @Override
            public void onBegin() {

            }

            @Override
            public void onSuccess() {
                final SubmissionResult result = task.getResult();

                if (result == null) {
                    return;
                }
                String exerciseName = task.getProject().getExercise().getName();
                Exercise exercise = task.getProject().getExercise();
                uiInvoker.invokeTestResultWindow(result.getTestCases());

                if (result.allTestCasesSucceeded()) {
                    uiInvoker.invokeAllTestsPassedWindow(result, exerciseName);
                    exercise.setCompleted(true);
                } else if (result.allTestCasesFailed()) {
                    uiInvoker.invokeAllTestsFailedWindow(result, exerciseName);
                    exercise.setAttempted(true);
                } else {
                    uiInvoker.invokeSomeTestsFailedWindow(result, exerciseName);
                    exercise.setAttempted(true);
                }
                // projectIconHandler.updateIcon(exercise); // TODO - Implement
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onInterruption() {

            }
        };
        Core.getTaskRunner().runTask(task, taskListener);
    }
}
