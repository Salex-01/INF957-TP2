package Q1;

import common.CloserListener;
import common.Terminable;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class PigeonMap extends Canvas implements Terminable {

    // Dimensions de la carte
    int height;
    int width;

    // Tableaux stockant la nourriture et les pigeons
    final LinkedList<Food> foodList;
    ArrayList<Pigeon> pigeonList;

    // Taille de la nourriture et des pigeons pour l'affichage
    int rangeFood = 10;
    int rangePigeon = 30;

    Mouse ms;   // MouseListener pour savoir où faire apparaitre la nourriture
    Random rand;

    double threshold;   // Probabilité des pigeons de se faire effrayer

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

    // Ajoute de la nourriture aux coordonnées indiquées
    @SuppressWarnings("all")
    public void addFood(int x, int y) {
        int size = foodList.size();
        synchronized (foodList) {
            foodList.addFirst(new Food(x, y));
        }
        // Réveille les threads des pigeons si ils étaient en attente
        if (size == 0) {
            for (Pigeon pigeon : pigeonList
            ) {
                synchronized (pigeon) {
                    pigeon.notify();
                }
            }
        }
    }

    // Supprime la nourriture du terrain
    public boolean deleteFood(Food food) {
        synchronized (foodList) {
            return foodList.remove(food);
        }
    }

    // Ajoute un pigeon
    public void addPigeon(Pigeon pigeon) {
        pigeonList.add(pigeon);
    }

    // Récupère la nourriture la plus fraîche
    Food getFreshestFood() {
        synchronized (foodList) {
            if (!foodList.isEmpty()) {
                return foodList.getFirst();
            }
        }
        return null;
    }

    // True si il n'y pas de nourriture
    public boolean hasNoFood() {
        return foodList.isEmpty();
    }

    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public static void main(String[] args) {
        // Initialise l'affichage
        Frame frame = new Frame("Pigeon Feeder Simulator");
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(d.width / 10, d.height / 10, (int) (d.width * 0.8), (int) (d.height * 0.8));
        PigeonMap map = new PigeonMap(0, 0, frame.getWidth(), frame.getHeight(), frame);
        frame.add(map);
        frame.pack();

        frame.addWindowListener(new CloserListener(frame, map));
        frame.setVisible(true);

        // Crée et lance le nombre de pigeons passé en argument
        for (int i = 0; i < Integer.parseInt(args[0]); i++) {
            (new Pigeon(map)).start();
        }

        while (true) {
            // Supprime la nourriture trop vieille
            synchronized (map.foodList) {
                map.foodList.removeIf(food -> System.currentTimeMillis() - food.creation > Food.MAX_FOOD_AGE);
            }

            // Récupère un nouveau nombre aleatoire pour savoir si les pigeons seront effrayés ou non
            if (map.threshold < map.rand.nextDouble()) {
                // Les pigeons sont effrayés et la probabilité est réinitialisée
                map.scarePigeons();
                map.threshold = 1;
            } else {
                // Sinon, la probabilité pour effrayer les pigeons augmente
                map.threshold *= 0.99999;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            // Met à jour l'affichage
            map.repaint();
        }

    }

    // Déplace aléatoirement tout les pigeons
    void scarePigeons() {
        for (Pigeon pigeon : pigeonList
        ) {
            pigeon.scare();
        }
    }

    public void paint(Graphics g) {
        // Les pigeons sont des cercles gris
        g.setColor(Color.GRAY);
        for (Pigeon pigeon : pigeonList
        ) {
            g.fillOval((int) pigeon.posX - rangePigeon / 2, (int) pigeon.posY - rangePigeon / 2, rangePigeon, rangePigeon);
        }

        // La nourriture des carrés verts
        g.setColor(Color.GREEN);
        synchronized (foodList) {
            for (Food food : foodList
            ) {
                g.fillRect((int) food.x - rangeFood / 2, (int) food.y - rangeFood / 2, rangeFood, rangeFood);
            }
        }
    }

    // Permet d'afficher les scores finaux de chaque pigeon
    public void terminate(){
        for (Pigeon p : pigeonList) {
            System.out.println(p);
        }
    }
}