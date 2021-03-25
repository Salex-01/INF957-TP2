package Q1;

import java.util.Random;


public class Pigeon extends Thread{

    PigeonMap map;
    float radius;

    float posX;
    float posY;

    long timeSinceStart;
    long oldTimeSinceStart;
    float deltatime;

    Pigeon(PigeonMap map) {

        this.map = map;
        radius = map.rangePigeon/2;
        map.addPigeon(this);
        Random rand = new Random();
        posX = rand.nextInt(map.width + 1);
        posY = rand.nextInt(map.height + 1);

        oldTimeSinceStart = System.nanoTime();
        deltatime = 0;
    }

    @Override
    public void run() {
        try {
            while (true) {
                timeSinceStart = System.nanoTime() ;
                deltatime = (float) ((timeSinceStart - oldTimeSinceStart) / Math.pow(10,7));
                System.out.println(deltatime);
                oldTimeSinceStart = timeSinceStart;

                if (map.hasNoFood()) {
                    System.out.println("no food pigeon is waiting");
                    synchronized (this) {
                        this.wait();
                    }
                }
                else {
                    this.moveToFreshestFood();
                    map.repaint();

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void moveToFreshestFood() throws InterruptedException {
        Food freshestFood = map.getFreshestFood();
        float magnitude =(float) Math.sqrt(Math.pow(this.posX - freshestFood.x,2) + Math.pow(this.posY - freshestFood.y,2));
        this.posX += deltatime * (freshestFood.x - this.posX)/magnitude;
        this.posY += deltatime * (freshestFood.y - this.posY)/magnitude;
        if (magnitude < radius) {
            this.eatFood(freshestFood);
        }
    }


    synchronized void eatFood(Food food) {
        map.deleteFood(food);
        map.repaint();
    }
}
