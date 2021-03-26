import java.awt.*;

public class GraphCanvas extends Canvas {
    Main4 m4;

    public GraphCanvas(Container c, Main4 m, boolean b) {
        if (b) {
            setBounds((int) (c.getWidth() * 0.1 + 5), 5, (int) (c.getWidth() * 0.9 - 10), c.getHeight() - 10);
        } else {
            setBounds(5, 5, c.getWidth() - 10, c.getHeight() - 10);
        }
        c.add(this);
        m4 = m;
    }
}