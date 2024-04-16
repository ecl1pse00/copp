package org.example.models;

import org.example.Controllers.*;
import org.example.Interface.MyApp;

import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class SearchThread extends SwingWorker <Map<Integer, List<Integer>>, Void> {
    private final FileUtils fileUtils;
    private final Map<Integer, List<Byte>> pageBuffer;
    private final RandomAccessFile randomAccessFile;
    private final char[] pattern;
    private final DialogWait wait;
    private final Map<Integer, List<Integer>> highlightCells = new HashMap<>();

    public SearchThread(MyApp myApp, DialogWait wait, char[] pattern) {
        this.pattern = pattern;
        this.wait = wait;
        fileUtils = myApp.getFileUtils();
        pageBuffer = fileUtils.getPageBuffer();
        randomAccessFile = fileUtils.getRandomAccessFile();
    }

    @Override
    protected Map<Integer, List<Integer>> doInBackground() throws Exception {
        int countPage = 0;
        int counterPageBuf = 0;
        int counterByteOnPage = 0;
        randomAccessFile.seek(0);

        if (pattern == null || pattern.length == 0) {
            JOptionPane.showMessageDialog(null, "Шаблон пустой");
            return null;
        }

        if (pattern.length > fileUtils.getFileSize()) {
            JOptionPane.showMessageDialog(null, "Длина текста меньше длины шаблона");
            return null;
        }

        for (int j = 0; countPage < fileUtils.getCounterPage();) {
            String value;
            if (pageBuffer.containsKey(countPage)) {
                value = String.format("%02X", pageBuffer.get(countPage).get(counterPageBuf));
                counterPageBuf += 1;
            } else {
                value = String.format("%02X", randomAccessFile.readByte());
                counterByteOnPage += 1;
            }

            char[] symbols = value.toCharArray();
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
                if (highlightCells.containsKey(countPage)){
                    highlightCells.get(countPage).add(counterByteOnPage + counterPageBuf - pattern.length / 2);
                } else {
                    List<Integer> index = new ArrayList<>();
                    index.add(counterByteOnPage + counterPageBuf - pattern.length / 2);
                    highlightCells.put(countPage, index);
                }
                j = 0;
            }

//            if (j < pattern.size() && (pattern.get(j).equals("?") || value.equals(pattern.get(j)))) {
//                if (++j == pattern.size()) {
//                    if (highlightCells.containsKey(countPage)){
//                        highlightCells.get(countPage).add(counterByteOnPage + counterPageBuf - pattern.size());
//                    } else {
//                        List<Integer> index = new ArrayList<>();
//                        index.add(counterByteOnPage + counterPageBuf - pattern.size());
//                        highlightCells.put(countPage, index);
//                    }
//                    j = 0;
//                }
//            } else if (j > 0) {
//                j = 0;
//            }

            if (pageBuffer.containsKey(countPage)) {
                if (counterPageBuf == pageBuffer.get(countPage).size()) {
                    counterPageBuf = 0;
                    randomAccessFile.seek(randomAccessFile.getFilePointer() + FileUtils.commonSizePage);  // Баг остался почему-то после удаления части байтов указатель первый с конца стоит не на правильном месте
                    countPage += 1;
                }
            } else {
                if (counterByteOnPage == FileUtils.commonSizePage || fileUtils.getFileSize() - countPage * 1024L == counterByteOnPage) {
                    counterByteOnPage = 0;
                    countPage += 1;
                }
            }

            if (isCancelled()) {
                return null;
            }
        }

        wait.close();
        return highlightCells;
    }

}
