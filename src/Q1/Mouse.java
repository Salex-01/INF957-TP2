package Q1;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Mouse implements MouseListener {

    PigeonMap map;
    Frame f;

    public Mouse(PigeonMap map, Frame f1) {
        this.map = map;
        f = f1;
    }

    // Crée de la nourriture aux coordonnées de la souris
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        map.addFood(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    // Redimensionne la carte si la fenêtre est redimensionnée
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        map.setBounds(0,0,f.getWidth(),f.getHeight());
        map.height = f.getHeight();
        map.width = f.getWidth();
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }
}