package fi.iki.dezgeg.tmc.idea.gui;

import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.TestCaseResult;
import fi.helsinki.cs.tmc.core.ui.IdeUIInvoker;

import java.util.List;

public class IdeaUIInvoker implements IdeUIInvoker {
    @Override
    public void invokeTestResultWindow(List<TestCaseResult> testCaseResults) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeAllTestsPassedWindow(SubmissionResult submissionResult, String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeSomeTestsFailedWindow(SubmissionResult submissionResult, String s) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void invokeAllTestsFailedWindow(SubmissionResult submissionResult, String s) {
        throw new RuntimeException("Not implemented!");
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
