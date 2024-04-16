package org.example.models;

import org.example.Controllers.TableController;
import javax.swing.JTable;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HexTable extends JTable {
    private final TableController tableController;
    private final HexTableModel hexTableModel;
    public static final int columnSize = 25;
    private int sumFillCells;
    private int address = 0;
    private boolean tableEmpty = true;
    private boolean canEdited = false;

    public HexTable(TableController tableController, HexTableModel hexTableModel) {
        super(hexTableModel);
        this.hexTableModel = hexTableModel;
        this.tableController = tableController;

        this.getColumnModel().setColumnSelectionAllowed( true );
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = this.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(columnSize);
        }

        if (tableEmpty && this.getRowCount() > 0) {
            tableController.setMyTable(this);
            selectByte(address);
            tableEmpty = false;
            canEdited = false;
        }

        JPopupMenu popupMenu = new MyPopupMenu(tableController);
        this.setComponentPopupMenu(popupMenu);

        this.addMouseListener(new MouseAdapter() { // Чтение информации выделенных мышкой байт
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    address = HexTable.this.getSelectedRow() * HexTable.this.getColumnCount() + HexTable.this.getSelectedColumn();
                    tableController.takeByteInfo();
                }
            }
        });
    }

    public void selectByte(int ad) {
        setAddress(ad);
        HexTable.this.changeSelection(address / this.getColumnCount(), address % this.getColumnCount(), false, false);
        tableController.takeByteInfo();
    }

    public int getSumFillCells() {
        return sumFillCells;
    }

    public void setSumFillCells(int sumFillCells) {
        this.sumFillCells = sumFillCells;
    }

    public void setAddress(int address) {
        if (address > sumFillCells) {
            address = sumFillCells - 1;
        }
        this.address = address;
    }

    public int getAddress() {
        address = this.getSelectedRow() * this.getColumnCount() + this.getSelectedColumn();
        if (address < 0) {
            address = 0;
        }
        return address;
    }

    public HexTableModel getHexTableModel() {
        return hexTableModel;
    }

    public void setTableEmpty(boolean tableEmpty) {
        this.tableEmpty = tableEmpty;
    }

    public boolean isCanEdited() {
        return canEdited;
    }

    public void setCanEdited(boolean canEdited) {
        this.canEdited = canEdited;
    }
}
