import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButtonManual extends GraphButton {
    public MessageButtonManual(Container c1, Main4 m) {
        super(c1, m, 0);
        setLabel("Envoi manuel");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String str = JOptionPane.showInputDialog("Nombre de messages", "1");
        if (str != null) {
            int n = Integer.parseInt(str);
            int a = -1;
            int b = -1;
            str = JOptionPane.showInputDialog("ID de la source (-1 pour aléatoire)", "1");
            if (str != null) {
                a = Integer.parseInt(str);
            }
            str = JOptionPane.showInputDialog("ID de la destination (-1 pour aléatoire)", "2");
            if (str != null) {
                b = Integer.parseInt(str);

            }
            sendMessages(n, a, b);
        }
    }
}