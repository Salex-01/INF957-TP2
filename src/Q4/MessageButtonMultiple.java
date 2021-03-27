package Q4;

import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageButtonMultiple extends MessageButton {
    int n;

    public MessageButtonMultiple(Container c1, Main4 m, int n1) {
        super(c1, m, (int) Math.log10(n1) + 2);
        n = n1;
        setLabel(n1 + (n1 > 1 ? " aléatoires" : " aléatoire"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setBackground(Color.BLACK);
        sendMessages(n, -1, -1);
        setBackground(Color.GRAY);
    }
}