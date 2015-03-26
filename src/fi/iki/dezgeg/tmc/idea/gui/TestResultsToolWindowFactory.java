package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import fi.helsinki.cs.tmc.core.domain.TestCaseResult;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class TestResultsToolWindowFactory implements ToolWindowFactory {
    private JPanel rootPane;
    private JProgressBar progressBar;
    private JScrollPane testResultsScrollPane;
    private JPanel testResultsPane;

    private static TestResultsToolWindowFactory instance;
    private static List<TestCaseResult> shownResults; // XXX hack - remove

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // XXX: why can't BoxLayout be specified in the form creator?
        testResultsPane.setLayout(new BoxLayout(testResultsPane, BoxLayout.Y_AXIS));

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(rootPane, "", false);
        toolWindow.getContentManager().addContent(content);
        if (instance != null) {
            throw new RuntimeException("TestResultsToolWindowFactory instance already exists?");
        }
        instance = this;
        doShowTestResults();
    }

    public void doShowTestResults() {
        testResultsPane.removeAll();
        for (TestCaseResult result : shownResults) {
            TestResultWidget widget = new TestResultWidget().init(result);
            testResultsPane.add(widget.getRootWidget());
        }
        testResultsPane.revalidate();
        testResultsPane.repaint();
    }

    public static void showTestResults(Project project, List<TestCaseResult> testCaseResults) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // XXX - this is a huge hack
            shownResults = testCaseResults;
            ToolWindowManager.getInstance(project).getToolWindow("TMC test results").activate(null);
            if (instance != null) {
                instance.doShowTestResults();
            }
        });
    }
}
