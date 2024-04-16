package org.example.Controllers;

import org.example.Interface.MyApp;
import org.example.models.DialogWait;
import org.example.models.HexTable;
import org.example.models.SearchThread;

import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class SearchController {
    private final MyApp myApp;
    private HexTable myTable;
    private boolean workSearch = false;
    private int indexHighlightArray = 0;
    private int indexHighlight = 0;
    private char[] pattern;
    private Map<Integer, List<Integer>> highlightCells = new HashMap<>();
    private List<Integer> keyArray;

    public SearchController(MyApp myApp) {
        this.myApp = myApp;
    }

    public void preparePattern(String message) {
        pattern = message.toCharArray();
        myApp.getFileUtils().bufferingPage();

        doSearch();
    }

    public void doSearch(){
        if (workSearch) {
            removeButton();
        }

        DialogWait wait = new DialogWait();
        SearchThread preparingThread = new SearchThread(myApp, wait, pattern);
        preparingThread.execute();
        wait.makeWait("Test", preparingThread);

        try {
            if (wait.isUserUnCloseWindow()) {
                highlightCells = preparingThread.get();
                if (highlightCells != null && highlightCells.size() > 0) {
                    workSearch = true;
                    scrollSearch();
                } else {
                    JOptionPane.showMessageDialog(null, "К сожалению, в таблице объект вашего поиска отсутствует.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Процесс поиска был прерван.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scrollSearch() {
        keyArray = new ArrayList<>(highlightCells.keySet());
        Collections.sort(keyArray);
        myApp.getFileUtils().setCurrentPage(keyArray.get(indexHighlight));
        myTable.selectByte(highlightCells.get(keyArray.get(indexHighlight)).get(indexHighlightArray));
        myApp.getPanelWithButton().setVisible(true);
    }

    public void addFunctionalUpButton(JButton up) {
        up.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                indexHighlightArray = (indexHighlightArray - 1);

                if (indexHighlightArray == - 1) {
                    indexHighlight = (indexHighlight - 1 + keyArray.size()) % keyArray.size();
                    myApp.getFileUtils().setCurrentPage(keyArray.get(indexHighlight));
                    indexHighlightArray = highlightCells.get(keyArray.get(indexHighlight)).size() - 1;
                } else if (highlightCells.containsKey(myApp.getFileUtils().getCurrentPage())) {
                    myApp.getFileUtils().setCurrentPage(keyArray.get(indexHighlight));
                }
                myTable.selectByte(highlightCells.get(keyArray.get(indexHighlight)).get(indexHighlightArray));
            }
        });
    }

    public void updateHighlightCell(List<Integer> newResult) {
        int index = myApp.getFileUtils().getCurrentPage();
        if (newResult == null || newResult.size() == 0) {
            highlightCells.remove(index);
            if (highlightCells.isEmpty()) {
                removeButton();
                return;
            }
        } else {
            highlightCells.put(index, newResult);
        }
        keyArray = new ArrayList<>(highlightCells.keySet());
        Collections.sort(keyArray);
        if (keyArray.contains(index)) {
            indexHighlight = keyArray.indexOf(index);
        } else {
            for (Integer pageValue : keyArray) {
                if (pageValue < index) {
                    indexHighlight = pageValue;
                } else {
                    break;
                }
            }
        }
        indexHighlightArray = 0;
    }

    public void addFunctionalDownButton(JButton down) {
        down.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                indexHighlightArray = (indexHighlightArray + 1);

                if (indexHighlightArray == highlightCells.get(keyArray.get(indexHighlight)).size()) {
                    indexHighlight = (indexHighlight + 1 + keyArray.size()) % keyArray.size();
                    myApp.getFileUtils().setCurrentPage(keyArray.get(indexHighlight));
                    indexHighlightArray = 0;
                } else if (highlightCells.containsKey(myApp.getFileUtils().getCurrentPage())) {
                    myApp.getFileUtils().setCurrentPage(keyArray.get(indexHighlight));
                }
                myTable.selectByte(highlightCells.get(keyArray.get(indexHighlight)).get(indexHighlightArray));
            }
        });
    }

    public void addFunctionalCloseButton(JButton close) {
        close.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeButton();
            }
        });
    }

    private void removeButton() {
        workSearch = false;
        myApp.getPanelWithButton().setVisible(false);
        myApp.getContainer().revalidate();
        highlightCells.clear();
        indexHighlight = 0;
        indexHighlightArray = 0;
    }

    public void setMyTable(HexTable myTable) {
        this.myTable = myTable;
    }

    public boolean isWorkSearch() {
        return workSearch;
    }

    public char[] getPattern() {
        return pattern;
    }
}
