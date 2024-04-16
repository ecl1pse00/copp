package org.example.Controllers;

import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;

public class LabelOperation {
    private final JLabel maxPageLabel = new JLabel("0");
    private JTextField pageInfo;

    public LabelOperation() {
    }

    public JTextField createCurrentPageInfo() {
        pageInfo = new JTextField();
        pageInfo.setPreferredSize(new Dimension(60, 20));

        return pageInfo;
    }

    public JLabel getMaxPageLabel() {
        return maxPageLabel;
    }

    public JTextField getPageInfo() {
        return pageInfo;
    }
}
