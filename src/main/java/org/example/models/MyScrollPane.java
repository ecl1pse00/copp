package org.example.models;

import javax.swing.JScrollPane;
import java.awt.Component;

public class MyScrollPane extends JScrollPane {

    public MyScrollPane(HexTable table, Component rowHeaders) {
        super(table);

        this.setRowHeaderView(rowHeaders);

    }
}

