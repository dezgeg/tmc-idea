package fi.iki.dezgeg.tmc.idea.gui;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import fi.iki.dezgeg.tmc.api.TmcApi;
import fi.iki.dezgeg.tmc.api.TmcException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AccountConfigStep extends CourseWizardStep {
    private JPanel panel;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JComboBox<String> serverCombobox;

    public AccountConfigStep(CourseWizard wizard) {
        super(wizard);
        usernameTextField.getDocument().addDocumentListener(wizard.updateButtonsListener);
        passwordTextField.getDocument().addDocumentListener(wizard.updateButtonsListener);

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        for (Pair<String, String> pair : TmcApi.DEFAULT_SERVERS) {
            model.addElement(String.format("%s (%s)", pair.first, pair.second));
        }
        serverCombobox.setModel(model);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean validate() {
        wizard.tmcApi.setCredentials(TmcApi.DEFAULT_SERVERS.get(serverCombobox.getSelectedIndex()).second,
                usernameTextField.getText(), new String(passwordTextField.getPassword()));
        try {
            wizard.courseList = wizard.tmcApi.getCourses();
        } catch (TmcException tmce) {
            tmce.printStackTrace();
            Messages.showErrorDialog(tmce.getMessage(), "Error logging in to TMC");
            wizard.courseList = null;
            return false;
        }
        return true;
    }

    @Override
    public boolean canGoNext() {
        return !usernameTextField.getText().isEmpty() &&
                passwordTextField.getPassword().length != 0;
    }
}