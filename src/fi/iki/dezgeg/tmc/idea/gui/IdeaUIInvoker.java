package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.TestCaseResult;
import fi.helsinki.cs.tmc.core.ui.IdeUIInvoker;

import java.util.List;

public class IdeaUIInvoker implements IdeUIInvoker {
    @Override
    public void invokeTestResultWindow(List<TestCaseResult> testCaseResults) {
        TestResultsToolWindowFactory.getInstance().showTestResults(testCaseResults);
    }

    @Override
    public void invokeAllTestsPassedWindow(SubmissionResult submissionResult, String exerciseName) {
        // TODO - show points etc.
        String messageStr = "Exercise " + exerciseName + " passed.";
        String title = "TODO - Replace this dialog";
        showMessage(messageStr, title);
    }

    @Override
    public void invokeSomeTestsFailedWindow(SubmissionResult submissionResult, String exerciseName) {
        String messageStr = "Exercise " + exerciseName + " failed.\n" + "Some tests failed on the server.\nSee Below";
        String title = "Some tests failed on server";
        showMessage(messageStr, title);
    }

    @Override
    public void invokeAllTestsFailedWindow(SubmissionResult submissionResult, String exerciseName) {
        String messageStr = "Exercise " + exerciseName + " failed.\n" + "All tests failed on the server.\nSee Below";
        String title = "All tests failed on server";
        showMessage(messageStr, title);
    }

    private void showMessage(final String messageStr, final String title) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Messages.showInfoMessage(messageStr, title);
            }
        });
    }

    @Override
    public void invokeSubmitToServerWindow() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeSendToPastebinWindow(String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokePastebinResultDialog(String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeRequestCodeReviewWindow(String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeCodeReviewRequestSuccefullySentWindow() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void raiseVisibleException(String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeCodeReviewDialog(Review review) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeMessageBox(String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeCodeReviewPopupNotification(List<Review> reviews) {
        throw new RuntimeException("Not implemented!");
    }
}
