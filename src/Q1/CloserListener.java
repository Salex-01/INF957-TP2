package Q1;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class CloserListener implements WindowListener {
    Frame f;
    PigeonMap m;

    public CloserListener(Frame f1, PigeonMap pm) {
        f = f1;
        m = pm;
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        // permet de stopper le programme quand la fenetre est fermee et d'afficher les scores finaux de chaque pigeon
        f.dispose();
        for (Pigeon p : m.pigeonList) {
            System.out.println(p);
        }
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