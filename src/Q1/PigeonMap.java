package Q1;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class PigeonMap extends Canvas {

    // dimension de la carte
    int height;
    int width;

    // tableaux stockant la nourriture et les pigeons
    final LinkedList<Food> foodList;
    ArrayList<Pigeon> pigeonList;

    // taille de la nourriture et des pigeons pour l'affichage
    int rangeFood = 10;
    int rangePigeon = 30;
    // un MouseListener pour savoir où faire apparaitre la nourriture
    Mouse ms;
    Random rand;

    // probabilite des pigeons de se faire effrayer
    double threshold;

    PigeonMap(int x, int y, int w, int h, Frame f) {
        setBounds(x, y, w, h);
        height = h;
        width = w;
        foodList = new LinkedList<>();
        pigeonList = new ArrayList<>();
        ms = new Mouse(this, f);
        addMouseListener(ms);
        rand = new Random();
        threshold = 1;
    }

    // ajoute de la nourriture aux coordonnees indiquees
    public void addFood(int x, int y) {
        int size = foodList.size();
        synchronized (foodList) {
            foodList.addFirst(new Food(x, y));
        }
        // reveille les threads des pigeons si il etait en attentes
        if (size == 0) {
            for (Pigeon pigeon : pigeonList
            ) {
                synchronized (pigeon) {
                    pigeon.notify();
                }
            }
        }
    }

    // supprime la nourriture du terrain
    public boolean deleteFood(Food food) {
        synchronized (foodList) {
            return foodList.remove(food);
        }
    }

    //ajoute un pigeon au tableau
    public void addPigeon(Pigeon pigeon) {
        pigeonList.add(pigeon);
    }

    // récupere la nourriture la plus fraiche
    Food getFreshestFood() {
        synchronized (foodList) {
            if (!foodList.isEmpty()) {
                return foodList.getFirst();
            }
        }
        return null;
    }

    // renvoie true si il n'y pas de nourriture
    public boolean hasNoFood() {
        return foodList.isEmpty();
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public static void main(String[] args) {
        // initialise l'affichage
        Frame frame = new Frame("Pigeon Feeder Simulator");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(d.width / 10, d.height / 10, (int) (d.width * 0.8), (int) (d.height * 0.8));
        PigeonMap map = new PigeonMap(0, 0, frame.getWidth(), frame.getHeight(), frame);
        frame.add(map);
        frame.pack();

        frame.addWindowListener(new CloserListener(frame, map));
        frame.setVisible(true);

        // ajoute le nombre de pigeon passe en argument a la carte
        for (int i = 0; i < Integer.parseInt(args[0]); i++) {
            (new Pigeon(map)).start();
        }

        while (true) {
            // supprime la nourriture trop vieille
            synchronized (map.foodList) {
                map.foodList.removeIf(food -> System.currentTimeMillis() - food.creation > Food.MAX_FOOD_AGE);
            }

            // recupere un nouveau nombre aleatoire pour savoir si les pigeons seront affrayes ou non
            if (map.threshold < map.rand.nextDouble()) {
                // les pigeons sont effrayes et la probabilite est reinitialise
                map.scarePigeons();
                map.threshold = 1;
            } else {
                // sinon la probabilité pour effraye les pigeons augmente
                map.threshold *= 0.99999;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            // met a jour l'affichage
            map.repaint();
        }

    }
    // deplace aleatoirement tout les pigeons de la carte
    void scarePigeons() {
        for (Pigeon pigeon : pigeonList
        ) {
            pigeon.scare();
        }
    }
    // realise l'affichage
    public void paint(Graphics g) {
        // les pigeons sont des cercles gris
        g.setColor(Color.GRAY);
        for (Pigeon pigeon : pigeonList
        ) {
            g.fillOval((int) pigeon.posX - rangePigeon / 2, (int) pigeon.posY - rangePigeon / 2, rangePigeon, rangePigeon);
        }

        // la nourriture des carre vert
        g.setColor(Color.GREEN);
        synchronized (foodList) {
            for (Food food : foodList
            ) {
                g.fillRect((int) food.x - rangeFood / 2, (int) food.y - rangeFood / 2, rangeFood, rangeFood);
            }
        }
    }
}
