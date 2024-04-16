package org.example.models;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;

public class MyListModel extends AbstractListModel<String> {
    private final List<String> data = new ArrayList<>();

    public void addAllElement(long startValue, int countColumn, int countRow) {
        removeAll();
        for (int i = 0; i < countRow; i++) {
            addElement(startValue + (long) i * countColumn);
        }
    }

    private void addElement(long value){
        data.add(String.format("%08X", value));
    }

    public void addElement(int countColumn) {
        long lastValue = Integer.parseInt(data.get(data.size() - 1), 16);
        addElement(lastValue + countColumn);
    }

    public void removeAll() {
        data.clear();
    }

    public void removeLast() {
        data.remove(data.size() - 1);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public String getElementAt(int index) {
        return data.get(index);
    }
}
