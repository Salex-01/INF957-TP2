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

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        // cree de la nourriture aux coordonnees de la souris
        map.addFood(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        // redimensionne la carte si la fenetre est redimentionnee
        map.setBounds(0,0,f.getWidth(),f.getHeight());
        map.height = f.getHeight();
        map.width = f.getWidth();
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
