import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ResizeListener implements ComponentListener {
    Container c;
    Frame f;

    public ResizeListener(Container c1, Frame f1) {
        c = c1;
        f = f1;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        c.setBounds(8, 30, f.getWidth() - 10, f.getHeight() - 30);
        for (Component co : c.getComponents()) {
            ((GraphComponent) co).resize();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
}