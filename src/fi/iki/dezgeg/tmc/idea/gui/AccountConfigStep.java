package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.ui.Messages;
import fi.iki.dezgeg.tmc.api.TmcApi;
import fi.iki.dezgeg.tmc.api.TmcException;

import javax.swing.*;

public class AccountConfigStep extends CourseWizardStep {
    private JPanel panel;
    private JTextField serverTextField;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;

    public AccountConfigStep(CourseWizard wizard) {
        super(wizard);
    }

    @Override
    public void _init() {
        super._init();
        serverTextField.setText(TmcApi.DEFAULT_SERVER_URL);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean validate() {
        wizard.tmcApi.setCredentials(usernameTextField.getText(), new String(passwordTextField.getPassword()));
        try {
            wizard.courseList = wizard.tmcApi.getCourses();
        } catch (TmcException tmce) {
            Messages.showErrorDialog(tmce.getMessage(), "Error logging in to TMC");
            wizard.courseList = null;
            return false;
        }
        return true;
    }
}
