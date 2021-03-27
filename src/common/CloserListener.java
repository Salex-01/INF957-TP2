package common;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class CloserListener implements WindowListener {
    Frame f;
    Terminable t;

    public CloserListener(Frame f1, Terminable t1) {
        f = f1;
        t = t1;
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        t.terminate();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {
    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {
    }
}