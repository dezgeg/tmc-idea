package fi.iki.dezgeg.tmc.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsDialog extends DialogWrapper {

    private JTextField serverTextField;
    private JTextField usernameTextField;
    private JTextField passwordTextField;
    private JComboBox courseCombobox;
    private JPanel panel;

    public SettingsDialog(@Nullable Project project) {
        super(project, false);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}
