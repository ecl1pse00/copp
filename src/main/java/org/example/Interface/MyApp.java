package org.example.Interface;

import org.example.Controllers.SearchController;
import org.example.Controllers.TableController;
import org.example.Controllers.LabelOperation;
import org.example.Controllers.FileUtils;
import org.example.Controllers.NiceFunction;
import org.example.models.MyScrollPane;
import org.example.models.HexTable;
import org.example.models.MyList;
import org.example.models.MyListModel;
import org.example.models.HexTableModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class MyApp extends JFrame {
    private SearchController searchController;
    private final TableController tableController;
    private final LabelOperation labelOperation;
    private MyScrollPane dataScrollPane;
    private HexTable hexTable;
    private final JLabel byteInfo;
    private final MyList rowHeaders;
    private final JPanel panelWithButton;
    private FileUtils fileUtils;
    private final Container container;
    private static final String patternUnHexString = "[^A-Fd0-9?]";
    private static final int startCountColumn = 16;

    public MyApp()
    {
        super("Hex-editor");
        container = getContentPane();
        byteInfo = new JLabel();
        labelOperation = new LabelOperation();
        tableController = new TableController(this);
        hexTable = createTable();
        tableController.setMyTable(hexTable);
        rowHeaders = new MyList(new MyListModel());
        dataScrollPane = new MyScrollPane(hexTable, rowHeaders);
        panelWithButton = createPanelWithButton();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isFileOpen() && !fileUtils.isFileSave()) {
                    int result = JOptionPane.showConfirmDialog(null, "Вы не сохранили файл. Уверены, что хотите закрыть приложение?",
                            "Предупреждение", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        System.exit(0);
                        setDefaultCloseOperation(EXIT_ON_CLOSE);
                    } else {
                        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    }
                } else {
                    setDefaultCloseOperation(EXIT_ON_CLOSE);
                }
            }
        });
        setSize(1200, 900);

        setJMenuBar(createMenuBar());

        container.add(byteInfo, BorderLayout.WEST);
        container.add(dataScrollPane, BorderLayout.CENTER);
        container.add(panelWithButton, BorderLayout.EAST);
        container.add(createPanelFlippingButton(), BorderLayout.SOUTH);

        setVisible(true);
        panelWithButton.setVisible(false);
    }

    private JPanel createPanelFlippingButton() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;
        constraints.gridy   = 0;
        constraints.gridx   = 0;
        JPanel panelFlippingButton = new JPanel();
        JButton left = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFileOpen()) {
                    fileUtils.flipLeft();
                }
            }
        });
        panelFlippingButton.add(left, constraints);

        constraints.gridx = 2;
        panelFlippingButton.add(labelOperation.createCurrentPageInfo(), constraints);

        constraints.gridx = 3;
        panelFlippingButton.add(labelOperation.getMaxPageLabel(), constraints);
        JButton right = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFileOpen()) {
                    try {
                        fileUtils.flipRight();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        constraints.gridx = 4;
        panelFlippingButton.add(right, constraints);

        return panelFlippingButton;
    }

    private HexTable createTable() {
        HexTableModel tempTableModel = new HexTableModel(NiceFunction.getColumnsName(startCountColumn), this);

        return new HexTable(tableController, tempTableModel);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        setJMenuBar(menuBar);

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");
        JMenuItem open = new JMenuItem("Open");
        open.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose file");

                if ( fileChooser.showOpenDialog(MyApp.this) == JFileChooser.APPROVE_OPTION ) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        hexTable.setCanEdited(false);
                        hexTable.getHexTableModel().setRowCount(0);
                        fileUtils = new FileUtils(file, hexTable.getHexTableModel(), MyApp.this);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        JMenuItem saveAs = new JMenuItem("Save as");
        saveAs.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFileOpen()) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save file");

                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File save = fileChooser.getSelectedFile();

                        try {
                            fileUtils.saveFile(save);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Откройте файл.");
                }
            }
        });
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFileOpen()) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save file");
                    try {
                        fileUtils.saveFile(fileUtils.getFile());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Откройте файл.");
                }
            }
        });

        file.add(open);
        file.add(save);
        file.add(saveAs);

        return file;
    }

    private JMenu createEditMenu() {
        JMenu edit = new JMenu("Edit");
        JMenuItem changeNumColumns = new JMenuItem("Сменить кол-во столбцов");
        changeNumColumns.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String getMessage = JOptionPane.showInputDialog(MyApp.this, "Enter your num");
                if (getMessage == null) return;

                if (getMessage.replaceAll("[^0-9]", "").equals(getMessage) && Integer.parseInt(getMessage) != 0) {
                    remove(dataScrollPane);
                    dataScrollPane = new MyScrollPane(tableController.changeTable(Integer.parseInt(getMessage)), rowHeaders);
                    hexTable = tableController.getMyTable();
                    if (fileUtils != null) {
                        fileUtils.setTableModel(tableController.getMyTable().getHexTableModel());
                    }
                    searchController.setMyTable(tableController.getMyTable());
                    container.add(new Container().add(dataScrollPane), BorderLayout.CENTER);
                    revalidate();
                }
            }
        });
        JMenuItem search = new JMenuItem("Search");
        search.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileUtils != null) {
                    String getMessage = JOptionPane.showInputDialog(MyApp.this, "Введите что необходимо найти:");
                    if (getMessage != null && getMessage.length() % 2 != 1) {
                        if (getMessage.replaceAll(patternUnHexString, "").equals(getMessage)) {
                            searchController.setMyTable(hexTable);
                            searchController.preparePattern(getMessage);
                        } else JOptionPane.showMessageDialog(null, "Ошибка строчки ввода или нечетное количество байт");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Откройте файл.");
                }
            }
        });

        edit.add(changeNumColumns);
        edit.add(search);
        return edit;
    }

    private JPanel createPanelWithButton() {
        JPanel panelWithButton = new JPanel(new GridLayout(3, 1));
        searchController = new SearchController(MyApp.this);


        JButton up = new JButton("Up");
        searchController.addFunctionalUpButton(up);

        JButton close = new JButton("Close");
        searchController.addFunctionalCloseButton(close);

        JButton down = new JButton("Down");
        searchController.addFunctionalDownButton(down);

        panelWithButton.add(up);
        panelWithButton.add(down);
        panelWithButton.add(close);

        return panelWithButton;
    }

    public void changeByteInfoLabel(String text) {
        byteInfo.setText(text);
        container.revalidate();
    }

    public Container getContainer() {
        return container;
    }

    public JPanel getPanelWithButton() {
        return panelWithButton;
    }

    public TableController getTableController() {
        return tableController;
    }

    public SearchController getSearchController() {
        return searchController;
    }

    public LabelOperation getLabelOperation() {
        return labelOperation;
    }

    public MyList getRowHeaders() {
        return rowHeaders;
    }

    public FileUtils getFileUtils() {
        return fileUtils;
    }

    public boolean isFileOpen() {
        return fileUtils != null;
    }

    public static void main(String[] args)
    {
        new MyApp();
    }
}
