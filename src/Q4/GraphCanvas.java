package Q4;

import java.awt.*;

public class GraphCanvas extends Canvas implements GraphComponent {
    Container c;

    public GraphCanvas(Container c1) {
        c = c1;
        setBounds((int) (c.getWidth() * 0.1 + 5), 5, (int) (c.getWidth() * 0.9 - 10), c.getHeight() - 10);
        c1.add(this);
    }

    @Override
    public void resize() {
        setBounds((int) (c.getWidth() * 0.1 + 5), 5, (int) (c.getWidth() * 0.9 - 10), c.getHeight() - 10);
    }
}