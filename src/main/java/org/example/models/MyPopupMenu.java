package org.example.models;

import org.example.Controllers.NiceFunction;
import org.example.Controllers.TableController;
import org.example.Interface.MyApp;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.math.BigInteger;

public class MyPopupMenu extends JPopupMenu {
    public MyPopupMenu(TableController tableController) {
        super();
        MyApp myApp = tableController.getMyApp();

        this.setBounds(200, 100, 150, 200);

        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener(new DeleteAction(myApp));

        JMenuItem add = new JMenuItem("Add");
        add.addActionListener(new AddAction(myApp));

        JMenuItem cut = new JMenuItem("Cut");
        cut.addActionListener(new CutAction(myApp));

        JMenuItem getInfo = new JMenuItem("Get bytes info");
        getInfo.addActionListener(new GetInfoAction(myApp));

        this.add(delete);
        this.add(add);
        this.add(cut);
        this.addSeparator();
        this.add(getInfo);
    }
}
class DeleteAction extends AbstractAction{
    private final MyApp myApp;

    public DeleteAction(MyApp myApp) {
        this.myApp = myApp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(null, "Удалить со смещением?");
        if (result == JOptionPane.NO_OPTION) {
            myApp.getTableController().deleteWithoutOffset();
        } else if (result == JOptionPane.YES_OPTION) {
            myApp.getTableController().deleteWithOffset();
        }
    }
}
class AddAction extends AbstractAction{
    private final MyApp myApp;

    public AddAction(MyApp myApp) {
        this.myApp = myApp;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(null, "Добавить со смещением?");

        if (result == JOptionPane.CANCEL_OPTION) {
            return;
        }

        if (result == JOptionPane.NO_OPTION) {
            String getMessage = JOptionPane.showInputDialog(null, "Введите что необходимо вставить:");
            if (getMessage != null && getMessage.replaceAll("[^A-Fa-f0-9]", "").equals(getMessage)) {
                myApp.getTableController().addWithoutOffset(getMessage);
            } else {
                JOptionPane.showMessageDialog(null, "Ваше сообщение не может быть вставлено.");
            }
        } else if (result == JOptionPane.YES_OPTION) {
            String getMessage = JOptionPane.showInputDialog(null, "Введите что необходимо вставить:");
            if (getMessage != null && getMessage.replaceAll("[^A-Fa-f0-9]", "").equals(getMessage)) {
                myApp.getTableController().addWithOffset(getMessage);
            } else {
                JOptionPane.showMessageDialog(null, "Ваше сообщение не может быть вставлено.");
            }
        }
    }
}
class CutAction extends AbstractAction{
    private final MyApp myApp;

    public CutAction(MyApp myApp) {
        this.myApp = myApp;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        myApp.getTableController().cutBytes();
    }
}
class GetInfoAction extends AbstractAction{
    private final MyApp myApp;

    public GetInfoAction(MyApp myApp) {
        this.myApp = myApp;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        String[] values = {"2", "4", "8"};

        Object selected = JOptionPane.showInputDialog(null, "How many bytes to read?", "Selection", JOptionPane.PLAIN_MESSAGE, null, values, "0");
        if (selected != null) {
            String selectedString = selected.toString();

            String bytes = myApp.getTableController().getSomeBytes(selectedString);
            if (bytes == null) {
                JOptionPane.showMessageDialog(null, "Не получается считать все байты");
                return;
            }
            float floatDecimal;

            switch (selectedString) {
                case "2":
                    int unsignedInteger = Integer.parseUnsignedInt(bytes, 16);
                    short signedShort = (short) NiceFunction.parseUnsignedHex(bytes, 4);
                    floatDecimal = Float.intBitsToFloat(signedShort);
                    JOptionPane.showMessageDialog(null, "Value: " + bytes + "\nUnsignedInteger: " + unsignedInteger +
                            "\nSignedInteger: " + signedShort + "\nFloat: " + floatDecimal);
                    break;
                case "4":
                    long unsignedLong = Long.parseUnsignedLong(bytes, 16);
                    int signedInteger = (int) NiceFunction.parseUnsignedHex(bytes, 8);
                    floatDecimal = Float.intBitsToFloat(signedInteger);
                    JOptionPane.showMessageDialog(null, "Value: " + bytes + "\nUnsignedInteger: " + unsignedLong +
                            "\nSignedInteger: " + signedInteger + "\nFloat: " + floatDecimal);
                    break;
                case "8":
                    BigInteger unsignedBigInteger = new BigInteger(bytes, 16);
                    long signedLong = NiceFunction.parseUnsignedHex(bytes, 16);
                    double doubleDecimal = Double.longBitsToDouble(signedLong);
                    JOptionPane.showMessageDialog(null, "Value: " + bytes + "\nUnsignedInteger: " + unsignedBigInteger +
                            "\nSignedInteger: " + signedLong + "\nDouble: " + doubleDecimal);
                    break;
            }
        }
    }
}
