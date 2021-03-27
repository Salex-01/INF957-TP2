package Q4;

import java.awt.*;
import java.awt.event.ActionListener;

// Bouton d'envoi de message
public abstract class MessageButton extends GraphButton {
    MessageButton(Container c1, Main4 m, int o) {
        super(c1, m, o);
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

    @Override
    public void resize() {
        setBounds(0, (c.getHeight() / 10) * order, c.getWidth() / 10, c.getHeight() / 10);
    }
}