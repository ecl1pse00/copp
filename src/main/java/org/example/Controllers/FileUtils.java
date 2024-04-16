package org.example.Controllers;

import org.example.Interface.MyApp;
import org.example.models.DialogWait;
import org.example.models.HexTableModel;
import org.example.models.SaveThread;

import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {
    private final File file;
    private final MyApp myApp;
    private final Map<Integer, List<Byte>> pageBuffer = new HashMap<>();
    private HexTableModel tableModel;
    private final RandomAccessFile randomAccessFile;
    private final int counterPage;
    private int currentPage = 0;
    public static final int commonSizePage = 1024;
    private boolean fileSave = true;
    private final long fileSize;
    public FileUtils(File file, HexTableModel tableModel, MyApp myApp) throws IOException {
        this.file = file;
        fileSize = file.length();
        this.myApp = myApp;
        this.tableModel = tableModel;
        counterPage = (int) (fileSize + commonSizePage - 1) / commonSizePage;
        myApp.getLabelOperation().getMaxPageLabel().setText(Integer.toString(counterPage - 1));
        randomAccessFile = new RandomAccessFile(file, "r");

        if (fileSize < commonSizePage) {
            tableModel.setRowCount((int) ((fileSize + tableModel.getColumnCount() - 1) / tableModel.getColumnCount()));
        } else {
            tableModel.setRowCount((commonSizePage + tableModel.getColumnCount() - 1) / tableModel.getColumnCount());
        }

        viewTable(0);

        myApp.getLabelOperation().getPageInfo().addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newValuePage = myApp.getLabelOperation().getPageInfo().getText();

                if (!newValuePage.equals("") && newValuePage.replaceAll("[^0-9]", "").equals(newValuePage)) {
                    setCurrentPage(Integer.parseInt(newValuePage));
                } else {
                    myApp.getLabelOperation().getPageInfo().setText(Integer.toString(currentPage));
                }
            }
        });
    }

    public int getCounterPage() {
        return counterPage;
    }

    public StringBuilder getSomeByte(StringBuilder info, int num) {
        if (currentPage < counterPage - 1) {
            if (pageBuffer.containsKey(currentPage + 1)) {
                for (int i = 0; i < num; i++) {
                    info.append(String.format("%02X", pageBuffer.get(currentPage + 1).get(i)));
                }
                return info;
            } else {
                try {
                    for (int i = 0; i < num; i++) {
                        info.append(String.format("%02X", randomAccessFile.readByte()));
                    }
                    randomAccessFile.seek(randomAccessFile.getFilePointer() - num);
                    return info;
                }  catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public void viewTable(long current) {
        int sizeList;
        if (pageBuffer.containsKey(currentPage)) {
            List<Byte> data = pageBuffer.get(currentPage);
            sizeList = data.size();

            for (int i = 0; i < data.size(); i++) {
                String symbol = String.format("%02X", data.get(i));
                tableModel.setValueAt(symbol, i / tableModel.getColumnCount(), i % tableModel.getColumnCount());
            }

            try {
                if (fileSize - current >= commonSizePage) {
                    randomAccessFile.seek(current + commonSizePage);
                } else {
                    randomAccessFile.seek(current + (fileSize - current));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (fileSize - current < commonSizePage) {
                sizeList = (int) (fileSize - current);
            } else {
                sizeList = commonSizePage;
            }

            String symbol;
            try {
                for (int i = 0; i < sizeList; i++) {
                    symbol = String.format("%02X", randomAccessFile.readByte());
                    tableModel.setValueAt(symbol, i / tableModel.getColumnCount(), i % tableModel.getColumnCount());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        myApp.getRowHeaders().getModel().addAllElement(getActualFilePosition(), tableModel.getColumnCount(), tableModel.getRowCount());
        myApp.getTableController().prepareTable(sizeList);
        myApp.getLabelOperation().getPageInfo().setText(Integer.toString(currentPage));
        myApp.getRowHeaders().repaint();
    }

    public void flipRight() throws IOException {
        if (currentPage < counterPage - 1) {
            bufferingPage();
            int currentListSize = commonSizePage;
            tableModel.setRowCount(0);
            if (pageBuffer.containsKey(currentPage + 1)) {
                currentListSize = pageBuffer.get(currentPage + 1).size();
            }

            long filePosition = randomAccessFile.getFilePointer();
            if (fileSize - filePosition < currentListSize) {
                tableModel.setRowCount((int) ((fileSize - filePosition + (tableModel.getColumnCount() - 1)) / tableModel.getColumnCount()));
            } else {
                tableModel.setRowCount((currentListSize + tableModel.getColumnCount() - 1) / tableModel.getColumnCount());
            }

            currentPage += 1;
            viewTable(filePosition);
        }
    }

    public void flipLeft() {
        if (currentPage > 0) {
            bufferingPage();
            int currentListSize = commonSizePage;
            tableModel.setRowCount(0);

            if (pageBuffer.containsKey(currentPage - 1)) {
                currentListSize = pageBuffer.get(currentPage - 1).size();
            }
            tableModel.setRowCount((currentListSize + tableModel.getColumnCount() - 1) / tableModel.getColumnCount());

            try {
                long temp = randomAccessFile.getFilePointer() - (long) (currentPage) * commonSizePage + commonSizePage;
                long position = randomAccessFile.getFilePointer() - temp;
                randomAccessFile.seek(position);
                currentPage -= 1;
                viewTable(position);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void bufferingPage() {
        List<Byte> value = myApp.getTableController().bufferingTable();
        myApp.getTableController().getMyTable().setCanEdited(false);
        if (value != null) {
            fileSave = false;
            pageBuffer.put(currentPage, value);
        }
    }

    public long getActualFilePosition() {
        long position = 0;
        for (int i = 0; i < currentPage; i++) {
            if (pageBuffer.containsKey(i)) {
                position += pageBuffer.get(i).size();
            } else {
                position += commonSizePage;
            }
        }
        return position;
    }

    public void setTableModel(HexTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }

    public Map<Integer, List<Byte>> getPageBuffer() {
        return pageBuffer;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setCurrentPage(int newValuePage) {
        if (counterPage > newValuePage && newValuePage >= 0) {
            try {
                bufferingPage();
                currentPage = newValuePage;
                int currentListSize = commonSizePage;
                tableModel.setRowCount(0);
                if (pageBuffer.containsKey(currentPage)) {
                    currentListSize = pageBuffer.get(currentPage).size();
                }

                long position = (long) currentPage * commonSizePage;
                randomAccessFile.seek(position);

                if (fileSize - position < currentListSize) {
                    tableModel.setRowCount((int) ((fileSize - position + (tableModel.getColumnCount() - 1)) / tableModel.getColumnCount()));
                } else {
                    tableModel.setRowCount((currentListSize + tableModel.getColumnCount() - 1) / tableModel.getColumnCount());
                }

                viewTable(position);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            myApp.getLabelOperation().getPageInfo().setText(Integer.toString(currentPage));
        }
    }

    public void saveFile(File file) throws IOException {
        File tempFile;
        SaveThread saveThread;
        DialogWait wait = new DialogWait();
        bufferingPage();
        if (file.equals(this.file)) {
            tempFile = new File(this.file.getParentFile() + File.separator + "temp" + this.file.getName());
            try {
                Files.copy(this.file.toPath(), tempFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            saveThread = new SaveThread(file, tempFile, myApp, wait);
        } else {
            randomAccessFile.seek(0);
            saveThread = new SaveThread(file, myApp, wait);
        }
        saveThread.execute();
        wait.makeWait("Не закрывайте окно. Идёт сохранение", saveThread);

        if (wait.isUserUnCloseWindow()) {
            try {
                if (saveThread.get()) {
                    JOptionPane.showMessageDialog(null, "Файл записан");
                    fileSave = true;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Процесс сохранения был прерван.");

        }
    }

    public boolean isFileSave() {
        return fileSave;
    }

    public File getFile() {
        return file;
    }

    public void setFileSave(boolean fileSave) {
        this.fileSave = fileSave;
    }
}
