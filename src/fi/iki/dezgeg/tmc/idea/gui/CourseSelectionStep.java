package fi.iki.dezgeg.tmc.idea.gui;

import javax.swing.*;

public class CourseSelectionStep extends CourseWizardStep {
    private JPanel panel;
    private JList<String> courseListbox;

    public CourseSelectionStep(CourseWizard wizard) {
        super(wizard);
    }

    @Override
    public void _init() {
        super._init();
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (String course : wizard.courseList.keySet()) {
            model.addElement(course);
        }
        courseListbox.setModel(model);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }
}
