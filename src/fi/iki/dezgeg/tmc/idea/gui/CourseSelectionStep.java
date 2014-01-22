package fi.iki.dezgeg.tmc.idea.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
        courseListbox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                wizard.updateButtonsListener.changedUpdate(null);
            }
        });
    }

    @Override
    public boolean canGoNext() {
        return courseListbox.getSelectedIndex() >= 0;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }
}
