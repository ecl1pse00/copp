package org.example.Controllers;

import org.example.Interface.MyApp;
import org.example.models.HexTable;
import org.example.models.HexTableModel;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class TableController {
    private final MyApp myApp;
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private DefaultTableModel tableModel;
    private HexTable myTable;
    private boolean tableChanged = false;
    public TableController(MyApp myApp) {
        this.myApp = myApp;
    }

    public void takeByteInfo() {
        int address = myTable.getAddress();
        Object cellValue = myTable.getValueAt(address / myTable.getColumnCount(), address % myTable.getColumnCount());
        if (cellValue != null) {
            String value = cellValue.toString();
            int byteInfo = Integer.valueOf(value, 16);
            myApp.changeByteInfoLabel("Value: " + byteInfo);
        } else {
            myApp.changeByteInfoLabel("Value: null");
        }
    }

    public void KMP(char[] pattern){
        List<Integer> highlightCells = new ArrayList<>();

        if (pattern.length > myTable.getSumFillCells() || myTable.getValueAt(0, 1) == null) {
            myApp.getSearchController().updateHighlightCell(null);
            return;
        }

        for (int i = 0, j = 0; i < myTable.getSumFillCells(); i++) {
            String tableValue = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount()).toString();
            char[] symbols = tableValue.toCharArray();
            for (char symbol : symbols) {
                if (pattern[j] == 'd' && Character.isDigit(symbol)) {
                    j++;
                    continue;
                }
                if (pattern[j] == '?' && Character.isLetter(symbol)) {
                    j++;
                    continue;
                }
                if (pattern[j] == symbol) {
                    j++;
                    continue;
                }
                j = 0;
                break;
            }
            if (j == pattern.length) {
                highlightCells.add(i - j / 2 + 1);
                j = 0;
            }
        }

        myApp.getSearchController().updateHighlightCell(highlightCells);
    }

    public HexTable changeTable(int numColumns) {
        myTable.setCanEdited(false);

        HexTableModel tempTableModel = new HexTableModel(NiceFunction.getColumnsName(numColumns), myApp);
        HexTable tempTable = new HexTable(this, tempTableModel);
        int address = myTable.getAddress();
        int i = 0;
        String[] row = new String[numColumns];

        if (tableModel.getRowCount() != 0 && tableModel.getValueAt(0, 1) != null) {
            tempTable.setTableEmpty(true);
            int temp = 0;
            for (; i < myTable.getSumFillCells(); i++) {
                Object value = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount());
                if (value != null) {
                    temp = i % numColumns;
                    if (temp == 0 && i != 0) {
                        tempTableModel.addRow(row);
                    }
                    row[temp] = value.toString();
                } else {
                    break;
                }
            }
            String[] lastRow = new String[temp + 1];
            System.arraycopy(row, 0, lastRow, 0, lastRow.length);
            tempTableModel.addRow(lastRow);

            tempTable.setSumFillCells(i);
            tempTable.selectByte(address);
            checkAndAddRow(tempTable.getSumFillCells() - 1);
            myApp.getRowHeaders().getModel().addAllElement(myApp.getFileUtils().getActualFilePosition(), tempTable.getColumnCount(), tempTable.getRowCount());
            myApp.revalidate();
        }

        tableModel = tempTableModel;
        myTable = tempTable;
        myTable.setCanEdited(true);
        return tempTable;
    }

    public void prepareTable(int sizePage) {
        tableChanged = false;
        checkAndAddRow(sizePage - 1);
        myTable.selectByte(0);
        myTable.setCanEdited(true);
        myTable.setTableEmpty(false);
        myTable.setSumFillCells(sizePage);
    }

    public List<Byte> bufferingTable() {
        List<Byte> tableData = new ArrayList<>();
        if (tableChanged) {
            for (int i = 0; i < myTable.getSumFillCells(); i++) {
                String value = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount()).toString();
                byte[] data = NiceFunction.convertHexToBytes(value);

                for (byte symbol: data) {
                    tableData.add(symbol);
                }
            }
        } else {
            return null;
        }
        return tableData;
    }

    public void addTableListenerFunctional(TableModelEvent e) {
        myTable.setCanEdited(false);
        tableChanged = true;
        myApp.getFileUtils().setFileSave(false);

        String tableValue = null;
        int startIndex = e.getLastRow() * myTable.getColumnCount() + e.getColumn();

        if (myTable.getValueAt(e.getLastRow(), e.getColumn()) == "") {
            myTable.setValueAt("00", e.getLastRow(), e.getColumn());
        } else {
            tableValue = myTable.getValueAt(e.getLastRow(), e.getColumn()).toString();
        }

        if (tableValue != null && (!tableValue.replaceAll("[^A-F0-9]", "").equals(tableValue)
                || tableValue.length() != 2)) {
            tableValue = tableValue.replaceAll("[^A-Fa-f0-9]", "").toUpperCase();

            if (tableValue.length() % 2 == 1) {
                tableValue = "0" + tableValue;
            }

            if (tableValue.length() != 0) {
                myTable.setValueAt(tableValue.substring(0, 2), e.getLastRow(), e.getColumn());
            } else {
                myTable.setValueAt("00", e.getLastRow(), e.getColumn());
            }
        }

        if (startIndex >= myTable.getSumFillCells()) {
            myTable.setSumFillCells(myTable.getSumFillCells() + 1);
            if (myTable.getValueAt(e.getLastRow(), (e.getColumn() + myTable.getColumnCount() - 1) % myTable.getColumnCount()) == null) {
                int addNulls = fillNullCells();
                myTable.setSumFillCells(myTable.getSumFillCells() + addNulls);
            }
        }

        checkAndAddRow(startIndex);
        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        myTable.setCanEdited(true);
    }

    public void deleteWithoutOffset() {
        myTable.setCanEdited(false);
        int[] indexSelectedRows = myTable.getSelectedRows();
        int[] indexSelectedCols = myTable.getSelectedColumns();

        for (int row: indexSelectedRows) {
            for (int col: indexSelectedCols) {
                if (myTable.getValueAt(row, col) != null)
                    myTable.setValueAt("00", row, col);
            }
        }

        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        tableChanged = true;
        myApp.getFileUtils().setFileSave(false);
        myTable.setCanEdited(true);
    }

    public void deleteWithOffset() {
        myTable.setCanEdited(false);
        int[] selectedRows = myTable.getSelectedRows();
        int[] selectedCols = myTable.getSelectedColumns();

        for (int row : selectedRows) {
            for (int col : selectedCols) {
                if (myTable.getValueAt(row, col) != null) {
                    myTable.setValueAt("DEL", row, col);
                }
            }
        }

        deleteExtraBytes(myTable.getSumFillCells() - 1, runOfTable());

        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        tableChanged = true;
        myApp.getFileUtils().setFileSave(false);
        myTable.setCanEdited(true);
    }

    public void addWithoutOffset(String message) {
        myTable.setCanEdited(false);
        message = message.toUpperCase();
        if (message.length() % 2 == 1) {
            message = "0" + message;
        }
        String[] res = message.split("(?<=\\G..)");

        int addNulls = fillNullCells();

        int addNewValue = fillWithoutOffset(res);

        myTable.setSumFillCells(myTable.getSumFillCells() + addNulls + addNewValue);

        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        tableChanged = true;
        myApp.getFileUtils().setFileSave(false);
        myTable.setCanEdited(true);
    }

    public void addWithOffset(String message) {
        myTable.setCanEdited(false);
        message = message.toUpperCase();
        if (message.length() % 2 == 1) {
            message = "0" + message;
        }
        String[] res = message.split("(?<=\\G..)");

        int startIndex = myTable.getSelectedRow() * myTable.getColumnCount() + myTable.getSelectedColumn();
        int counterRes = 0;

        if (myTable.getValueAt(startIndex / myTable.getColumnCount(), startIndex % myTable.getColumnCount()) != null) {

            for (int i = startIndex; i < myTable.getSumFillCells(); i++) {
                checkAndAddRow(i);

                String tableValue = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount()).toString();
                myTable.setValueAt(res[counterRes], i / myTable.getColumnCount(), i % myTable.getColumnCount());

                res[counterRes] = tableValue;
                counterRes = (counterRes + 1) % res.length;
            }

            for (int i = myTable.getSumFillCells(), counter = 0; counter < res.length; counter++, i++) {
                checkAndAddRow(i);

                myTable.setValueAt(res[counterRes], i / myTable.getColumnCount(), i % myTable.getColumnCount());
                counterRes = (counterRes + 1) % res.length;
            }

            myTable.setSumFillCells(myTable.getSumFillCells() + res.length);
        } else {
            int addNulls = fillNullCells();
            int addNewValues = fillWithoutOffset(res);

            myTable.setSumFillCells(myTable.getSumFillCells() + addNulls + addNewValues);
        }

        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        tableChanged = true;
        myApp.getFileUtils().setFileSave(false);
        myTable.setCanEdited(true);
    }

    public void cutBytes() {
        myTable.setCanEdited(false);

        int[] selectedRows = myTable.getSelectedRows();
        int[] selectedCols = myTable.getSelectedColumns();
        StringBuilder cutHex = new StringBuilder();

        for (int row : selectedRows) {
            for (int col : selectedCols) {
                if (myTable.getValueAt(row, col) != null) {
                    cutHex.append(myTable.getValueAt(row, col));
                    myTable.setValueAt("DEL", row, col);
                }
            }
        }
        StringSelection s = new StringSelection(cutHex.toString());
        clipboard.setContents(s, s);

        deleteExtraBytes(myTable.getSumFillCells() - 1, runOfTable());

        if (myApp.getSearchController().isWorkSearch()) {
            KMP(myApp.getSearchController().getPattern());
        }

        tableChanged = true;
        myTable.setCanEdited(true);
    }

    public String getSomeBytes(String selectedString) {
        int startIndex = myTable.getSelectedRow() * myTable.getColumnCount() + myTable.getSelectedColumn();
        StringBuilder bytes = new StringBuilder();
        int intEnd = Integer.parseInt(selectedString);

        if (startIndex + intEnd + 1 > myTable.getSumFillCells()) {
            intEnd = myTable.getSumFillCells() - (startIndex);
        }
        for (int i = startIndex, counter = 0; counter < intEnd; counter++, i++) {

            if (myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount()) != null) {
                String tableValue = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount()).toString();
                bytes.append(tableValue);
            } else {
                return null;
            }
        }
        if (bytes.length() / 2 != Integer.parseInt(selectedString)) {
            bytes = myApp.getFileUtils().getSomeByte(bytes, Integer.parseInt(selectedString) - bytes.length() / 2);
            if (bytes == null) {
                return null;
            }
        }

        return bytes.toString();
    }

    public int runOfTable (){
        int startIndex = myTable.getSelectedRow() * myTable.getColumnCount() + myTable.getSelectedColumn();
        int step = 0;

        for (int i = startIndex; i < myTable.getSumFillCells(); i++) {
            String tableValue = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount()).toString();

            if (tableValue.equals("DEL")) {
                step += 1;
            } else {
                myTable.setValueAt(tableValue, (i - step) / myTable.getColumnCount(), (i - step) % myTable.getColumnCount());
            }
        }

        return step;
    }

    private int fillWithoutOffset (String[] text) {
        int addNewValue = 0;
        int index = myTable.getSelectedRow() * myTable.getColumnCount() + myTable.getSelectedColumn();

        for (String bytes : text) {
            checkAndAddRow( index);

            if (tableModel.getValueAt(index / myTable.getColumnCount(), index % myTable.getColumnCount()) == null) {
                addNewValue += 1;
            }
            tableModel.setValueAt(bytes, index / myTable.getColumnCount(), index % myTable.getColumnCount());

            index++;
        }
        return addNewValue;
    }

    private int fillNullCells() {
        int startIndex = myTable.getSelectedRow() * myTable.getColumnCount() + myTable.getSelectedColumn();
        int counter = 0;
        for (int i = startIndex - 1; counter < myTable.getColumnCount() && i >= 0; counter++, i--) {
            Object value = myTable.getValueAt(i / myTable.getColumnCount(), i % myTable.getColumnCount());
            if (value == null) {
                myTable.setValueAt("00", i / myTable.getColumnCount(), i % myTable.getColumnCount());
            } else return counter;
        }
        return counter;
    }

    public void checkAndAddRow (int index) {
        if (index / myTable.getColumnCount() + 1 >= myTable.getRowCount() &&
                index % myTable.getColumnCount() == myTable.getColumnCount() - 1) {
            String[] temp = new String[myTable.getColumnCount()];
            tableModel.addRow(temp);
            myApp.getRowHeaders().getModel().addElement(temp.length);
        }
    }

    private void deleteExtraBytes(int lastIndex, int countExtra) {
        for (int i = lastIndex, counter = 0; counter < countExtra; counter++, i--) {
            myTable.setValueAt(null, i / myTable.getColumnCount(), i % myTable.getColumnCount());
        }
        deleteNullRow();
        myTable.setSumFillCells(myTable.getSumFillCells() - countExtra);
    }

    private void deleteNullRow() {
        for (int i = myTable.getRowCount() - 1; i > 0; i--)
            if (myTable.getValueAt(i - 1, myTable.getColumnCount() - 1) == null) {
                tableModel.removeRow(i);
                myApp.getRowHeaders().getModel().removeLast();
            }
        if (myTable.getValueAt(0, 1) == null) {
            myTable.setTableEmpty(true);
        }
    }

    public HexTable getMyTable() {
        return myTable;
    }

    public void setMyTable(HexTable myTable) {
        this.myTable = myTable;
        tableModel = (DefaultTableModel) myTable.getModel();
    }

    public boolean isCanEdited() {
        return myTable.isCanEdited();
    }

    public MyApp getMyApp() {
        return myApp;
    }
}

