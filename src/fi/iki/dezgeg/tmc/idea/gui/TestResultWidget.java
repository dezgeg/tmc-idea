package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.ui.JBColor;
import fi.helsinki.cs.tmc.core.domain.TestCaseResult;

import javax.swing.*;
import java.awt.*;

public class TestResultWidget {
    private JPanel rootWidget;
    private JPanel marginColorWidget;
    private JLabel testNameLabel;
    private JLabel errorMessageLabel;
    private JButton showMoreInfoButton;
    private JLabel moreInfoLabel;
    private JPanel buttonPanel;

    private TestCaseResult testCaseResult;

    public TestResultWidget init(TestCaseResult testCaseResult) {
        this.testCaseResult = testCaseResult;

        moreInfoLabel.setVisible(false);
        String prefix = null;
        if (testCaseResult.isSuccessful()) {
            marginColorWidget.setBackground(JBColor.GREEN);
            showMoreInfoButton.setVisible(false);
            prefix = "PASS: ";
        } else {
            marginColorWidget.setBackground(JBColor.RED);
            prefix = "FAIL: ";
        }
        testNameLabel.setText(prefix + testCaseResult.getName());
        errorMessageLabel.setText(testCaseResult.getMessage());

        return this;
    }

    public Component getRootWidget() {
        return rootWidget;
    }
}
