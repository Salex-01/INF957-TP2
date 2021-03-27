package Q4;

import java.awt.*;
import java.awt.event.ActionListener;

// Les boutons de l'interface
public abstract class GraphButton extends Button implements ActionListener, GraphComponent {
    Main4 m4;
    Container c;
    int order;

    GraphButton(Container c1, Main4 m, int o) {
        m4 = m;
        c = c1;
        order = o;
        addActionListener(this);
        setBounds(0, (c1.getHeight() / 10) * order, c1.getWidth() / 10, c1.getHeight() / 10);
        setBackground(Color.GRAY);
        c1.add(this);
    }

    @Override
    public void resize() {
        setBounds(0, (c.getHeight() / 10) * order, c.getWidth() / 10, c.getHeight() / 10);
    }
}