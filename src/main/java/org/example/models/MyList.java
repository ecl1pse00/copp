package org.example.models;

import javax.swing.JList;

public class MyList extends JList<String> {
    private final MyListModel model;
    private static final int cellTableHeight = 16;
    private static final int listWidthSize = 65;

    public MyList(MyListModel model) {
        super(model);
        this.model = model;

        this.setFixedCellWidth(listWidthSize);
        this.setFixedCellHeight(cellTableHeight);
    }

    @Override
    public MyListModel getModel() {
        return model;
    }
}
