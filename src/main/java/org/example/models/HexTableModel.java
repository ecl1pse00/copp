package org.example.models;


import org.example.Interface.MyApp;

import javax.swing.table.DefaultTableModel;

public class HexTableModel extends DefaultTableModel {
    public HexTableModel(String[] columnsName, MyApp myApp) {
        super(columnsName, 0);

        this.addTableModelListener(e -> {
            if (myApp.getTableController().isCanEdited()) {
                myApp.getTableController().addTableListenerFunctional(e);
            }
        });


    }
}
