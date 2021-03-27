package Q1;

import java.util.Random;


public class Pigeon extends Thread {

    PigeonMap map;
    // Rayon du pigeon
    double radius;

    // Position du pigeon sur la carte
    double posX;
    double posY;
    // Variables permettant de calculer deltatime
    long timeSinceStart;
    long oldTimeSinceStart;
    double deltatime;

    // Score du pigeon (dépend de la quantité de nourriture mangée)
    int score = 0;

    Random rand;
    static int id = 0;
    int ID = id++;

    Pigeon(PigeonMap map) {

        this.map = map;
        // Le rayon depend de la taille du pigeon à l'écran
        radius = map.rangePigeon / 2.;

        // Ajout du pigeon à la carte
        map.addPigeon(this);
        rand = new Random();
        // Initialisation aléatoire de la position
        posX = rand.nextInt(map.width + 1);
        posY = rand.nextInt(map.height + 1);

        // Iinitialisation du deltatime
        oldTimeSinceStart = System.nanoTime();
        deltatime = 0;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                // Mise à jour du deltatime
                timeSinceStart = System.nanoTime();
                deltatime = (timeSinceStart - oldTimeSinceStart) / Math.pow(10, 7);
                oldTimeSinceStart = timeSinceStart;

                // Si il n'y a pas de nourriture, le pigeon attend
                if (map.hasNoFood()) {
                    System.out.println("no food, pigeon is waiting");
                    synchronized (this) {
                        this.wait();
                        // Reset le timer a la fin du wait
                        timeSinceStart = System.nanoTime();
                        oldTimeSinceStart = timeSinceStart;
                    }
                } else {
                    // Bouge vers la nourriture la plus fraîche
                    this.moveToFreshestFood();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void moveToFreshestFood() {
        // Récupère la nourriture la plus fraîche de la carte
        Food freshestFood = map.getFreshestFood();

        if (freshestFood != null) {
            // Déplace le pigeon vers la nourriture si il y en a
            double magnitude = Math.sqrt(Math.pow(this.posX - freshestFood.x, 2) + Math.pow(this.posY - freshestFood.y, 2));
            this.posX += (Math.abs(deltatime * (freshestFood.x - this.posX) / magnitude) > Math.abs(freshestFood.x - this.posX) ?
                    (freshestFood.x - this.posX) : (deltatime * (freshestFood.x - this.posX) / magnitude));
            this.posY += (Math.abs(deltatime * (freshestFood.y - this.posY) / magnitude) > Math.abs(freshestFood.y - this.posY) ?
                    (freshestFood.y - this.posY) : (deltatime * (freshestFood.y - this.posY) / magnitude));
            if (magnitude < radius) {
                // Si le pigeon est assez proche, il essaye de manger la nourriture
                this.eatFood(freshestFood);
            }
        }
    }

    void eatFood(Food food) {
        if (map.deleteFood(food)) {
            // Si le pigeon a eu la nourriture, son score augmente de 1
            score++;
            System.out.println(this);
        }
    }

    // Fonction pour faire "peur" au pigeon,
    // Déplace le pigeon à une nouvelle position aléatoire
    void scare() {
        posX = rand.nextInt(map.width + 1);
        posY = rand.nextInt(map.height + 1);
    }

    @Override
    public String toString() {
        return "Pigeon " + ID + " : " + score;
    }
}