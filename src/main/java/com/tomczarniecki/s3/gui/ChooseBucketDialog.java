package com.tomczarniecki.s3.gui;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;
import com.tomczarniecki.s3.Pair;
import org.apache.commons.lang.StringUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import static com.tomczarniecki.s3.Pair.pair;

public class ChooseBucketDialog extends JDialog {

    private final JComboBox<String> bucketName;
    private final JTextField bucketPrefix;

    private boolean selected;

    public ChooseBucketDialog(List<String> availableBuckets) {
        setTitle("Choose Bucket");
        setModal(true);

        this.bucketName = new JComboBox<>(new Vector<>(availableBuckets));
        this.bucketPrefix = new JTextField();

        getContentPane().add(createDisplayPanel());
        setResizable(false);
        pack();
    }

    public Pair<String, String> get(String bucket, String prefix) {
        setValues(bucket, prefix);
        setLocationRelativeTo(null);
        setVisible(true);
        return result();
    }

    private JPanel createDisplayPanel() {
        String cols = "pref,5dlu,100dlu";
        String rows = "pref,5dlu,pref,5dlu,pref";

        return FormBuilder.create()
                .layout(new FormLayout(cols, rows))
                .addLabel("Bucket Name").xy(1, 1)
                .add(bucketName).xy(3, 1)
                .addLabel("Prefix").xy(1, 3)
                .add(bucketPrefix).xy(3, 3)
                .add(createButtonBar()).xyw(1, 5, 3)
                .padding(Paddings.DIALOG)
                .build();
    }

    private JPanel createButtonBar() {
        JButton createButton = new JButton("Create");
        createButton.addActionListener(new CreateAction());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelAction());

        return ButtonBarBuilder.create()
                .addGlue()
                .addButton(createButton)
                .addRelatedGap()
                .addButton(cancelButton)
                .build();
    }

    private void setValues(String bucket, String prefix) {
        selected = false;
        if (StringUtils.isNotEmpty(bucket)) {
            bucketName.setSelectedItem(bucket);
        }
        if (StringUtils.isNotEmpty(prefix)) {
            bucketPrefix.setText(prefix);
        }
    }

    private Pair<String, String> result() {
        if (selected) {
            String name = (String) bucketName.getSelectedItem();
            String prefix = StringUtils.trimToEmpty(bucketPrefix.getText());
            return pair(name, prefix);
        }
        return null;
    }

    private class CreateAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            selected = true;
            setVisible(false);
        }
    }

    private class CancelAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            selected = false;
            setVisible(false);
        }
    }
}
