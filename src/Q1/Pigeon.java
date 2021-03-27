package Q1;

import java.util.Random;


public class Pigeon extends Thread {

    PigeonMap map;
    // rayon du pigeon
    double radius;

    // position du pigeon sur la carte
    double posX;
    double posY;
    // variable permettant de calculer deltatime
    long timeSinceStart;
    long oldTimeSinceStart;
    double deltatime;

    // score du pigeon suivant de nombre de nourriture mangé
    int score = 0;

    Random rand;
    static int id = 0;
    int ID = id++;

    Pigeon(PigeonMap map) {

        this.map = map;
        // le rayon depends du rayon de la taille du dessein
        radius = map.rangePigeon / 2.;

        // ajout du pigeon à la carte des pigeons
        map.addPigeon(this);
        rand = new Random();
        // initialise aleatoirement la position initial
        posX = rand.nextInt(map.width + 1);
        posY = rand.nextInt(map.height + 1);

        // initialise le deltatime
        oldTimeSinceStart = System.nanoTime();
        deltatime = 0;
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                // mise à jour du deltatime
                timeSinceStart = System.nanoTime();
                deltatime = (timeSinceStart - oldTimeSinceStart) / Math.pow(10, 7);
                oldTimeSinceStart = timeSinceStart;

                // si il n'y a pas de nourriture le pigeon attends
                if (map.hasNoFood()) {
                    System.out.println("no food pigeon is waiting");
                    synchronized (this) {
                        this.wait();
                        // reset le timer a la fin du wait
                        timeSinceStart = System.nanoTime();
                        oldTimeSinceStart = timeSinceStart;
                    }
                } else {
                    // bouge vers la nourriture la plus fraiche
                    this.moveToFreshestFood();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void moveToFreshestFood() {
        // recupere la nourriture la plus fraiche de la carte
        Food freshestFood = map.getFreshestFood();

        if (freshestFood != null) {
            // deplace le pigeon si la nourriture n'est pas nulle
            double magnitude = Math.sqrt(Math.pow(this.posX - freshestFood.x, 2) + Math.pow(this.posY - freshestFood.y, 2));
            this.posX += (Math.abs(deltatime * (freshestFood.x - this.posX) / magnitude) > Math.abs(freshestFood.x - this.posX) ?
                    (freshestFood.x - this.posX) : (deltatime * (freshestFood.x - this.posX) / magnitude));
            this.posY += (Math.abs(deltatime * (freshestFood.y - this.posY) / magnitude) > Math.abs(freshestFood.y - this.posY) ?
                    (freshestFood.y - this.posY) : (deltatime * (freshestFood.y - this.posY) / magnitude));
            if (magnitude < radius) {
                // si le pigeon est assez proche il peut manger la nourriture
                this.eatFood(freshestFood);
            }
        }
    }

    void eatFood(Food food) {
        if (map.deleteFood(food)) {
            // si la nourriture est bien suprimée alors le score du pigeon augmente de 1
            score++;
            System.out.println(this);
        }
    }

    // fonction pour faire "peur" au pigeon, il choisit une nouvelle possition aleatoire sur la carte
    void scare() {
        posX = rand.nextInt(map.width + 1);
        posY = rand.nextInt(map.height + 1);
    }

    @Override
    public String toString() {
        return "Pigeon " + ID + " : " + score;
    }
}