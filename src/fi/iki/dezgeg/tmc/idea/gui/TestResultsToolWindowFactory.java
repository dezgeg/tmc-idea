package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import fi.helsinki.cs.tmc.core.domain.TestCaseResult;

import javax.swing.*;
import java.util.List;

public class TestResultsToolWindowFactory implements ToolWindowFactory {
    private JPanel rootPane;
    private JProgressBar progressBar;
    private JScrollPane testResultsScrollPane;
    private JPanel testResultsPane;

    private static TestResultsToolWindowFactory instance;

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // XXX: why can't BoxLayout be specified in the form creator?
        testResultsPane.setLayout(new BoxLayout(testResultsPane, BoxLayout.Y_AXIS));

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(rootPane, "", false);
        toolWindow.getContentManager().addContent(content);
        if (instance != null) {
            throw new RuntimeException("TestResultsToolWindowFactory instance already exists?");
        }
        instance = this;
    }

    public static TestResultsToolWindowFactory getInstance() {
        return instance;
    }

    public void showTestResults(final List<TestCaseResult> results) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                testResultsPane.removeAll();
                for (TestCaseResult result : results) {
                    TestResultWidget widget = new TestResultWidget().init(result);
                    testResultsPane.add(widget.getRootWidget());
                }
                testResultsPane.revalidate();
                testResultsPane.repaint();
            }
        });
    }
}
