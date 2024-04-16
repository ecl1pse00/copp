package org.example.models;


import javax.swing.SwingWorker;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Dialog;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DialogWait {
    private boolean userCloseWindow = false;
    private JDialog dialog;

    public void makeWait(String msg, SwingWorker thread) {

        dialog = new JDialog(null, msg, Dialog.ModalityType.APPLICATION_MODAL);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(new JLabel("Please wait......."), BorderLayout.PAGE_START);
        dialog.add(panel);
        dialog.pack();
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thread.cancel(true);
                userCloseWindow = true;
            }
        });
        dialog.setVisible(true);
    }

    public void close() {
        dialog.dispose();
    }

    public boolean isUserUnCloseWindow() {
        return !userCloseWindow;
    }
}
