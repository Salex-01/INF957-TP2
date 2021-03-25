import java.awt.*;
import java.awt.event.ActionListener;

public abstract class MessageButton extends Button implements ActionListener {
    Main4 m4;

    MessageButton(Container c1, Main4 m, int order) {
        m4 = m;
        addActionListener(this);
        setBounds(0, (c1.getHeight() / 10) * order, c1.getWidth() / 10, c1.getHeight() / 10);
        setBackground(Color.GRAY);
        c1.add(this);
    }

    protected void sendMessages(int n, int a, int b) {
        for (int i = 0; i < n; i++) {
            Node na = (a == -1 ? m4.g.getRandomNode() : m4.g.getNode(a));
            Node nb = (b == -1 ? m4.g.getRandomNode() : m4.g.getNode(b));
            if (na == nb) {
                if (a == -1) {
                    do {
                        na = m4.g.getRandomNode();
                    } while (na == nb);
                } else if (b == -1) {
                    do {
                        nb = m4.g.getRandomNode();
                    } while (na == nb);
                }
            }
            m4.g.sendMessage(na, nb);
        }
    }
}