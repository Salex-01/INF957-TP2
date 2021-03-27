import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class CloserListener implements WindowListener {
    Frame f;
    Graph g;

    public CloserListener(Frame f1, Graph g1) {
        f = f1;
        g = g1;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent event) {
        g.terminate();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}