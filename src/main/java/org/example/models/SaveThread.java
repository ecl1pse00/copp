package org.example.models;


import org.example.Controllers.FileUtils;
import org.example.Interface.MyApp;

import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class SaveThread extends SwingWorker<Boolean, Void> {
    private final File file;
    private File temp;
    private final FileUtils fileUtils;
    private final RandomAccessFile randomAccessFile;
    private final Map<Integer, List<Byte>> pageBuffer;
    private final DialogWait wait;

    public SaveThread(File file, File temp, MyApp myApp, DialogWait wait) {
        this.file = file;
        this.temp = temp;
        this.wait = wait;
        try {
            randomAccessFile = new RandomAccessFile(temp, "r");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        fileUtils = myApp.getFileUtils();
        pageBuffer = fileUtils.getPageBuffer();
    }

    public SaveThread(File file, MyApp myApp, DialogWait wait) {
        this.file = file;
        this.wait = wait;
        fileUtils = myApp.getFileUtils();
        pageBuffer = fileUtils.getPageBuffer();
        randomAccessFile = fileUtils.getRandomAccessFile();
    }

    @Override
    protected Boolean doInBackground() {
        try (OutputStream output = Files.newOutputStream(file.toPath()); BufferedOutputStream bos = new BufferedOutputStream(output)) {
            int countPage = 0;

            while (countPage < fileUtils.getCounterPage()) {
                byte[] data;
                if (pageBuffer.containsKey(countPage)) {
                    data = new byte[pageBuffer.get(countPage).size()];

                    for (int i = 0; i < pageBuffer.get(countPage).size(); i++) {
                        data[i] = pageBuffer.get(countPage).get(i);
                    }

                    bos.write(data);
                    randomAccessFile.seek(randomAccessFile.getFilePointer() + FileUtils.commonSizePage);
                } else {
                    long position = randomAccessFile.getFilePointer();
                    if (fileUtils.getFileSize() - position < 1024) {
                        data = new byte[(int) (fileUtils.getFileSize() - position)];
                    } else {
                        data = new byte[FileUtils.commonSizePage];
                    }

                    randomAccessFile.read(data);
                    bos.write(data);
                }
                countPage += 1;
            }

            if (isCancelled()) {
                if (temp != null) {
                    boolean delete = temp.delete();
                    randomAccessFile.close();
                } else if (file.delete()){
                    System.out.println("Файл удалён.");
                }
                return false;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Что-то не так. Файл не сохранен");
        }

        if (temp != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (temp.delete()) {
                System.out.println("File is deleted");
            }
        }
        wait.close();
        return true;
    }
}
