package Q1;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PigeonMap extends Canvas{

    int height;
    int width;
    ArrayList<Food> foodList;
    ArrayList<Pigeon> pigeonList;

    int rangeFood = 10;
    int rangePigeon = 30;
    Mouse ms;

    PigeonMap(int w, int h) {
        height = h;
        width = w;
        foodList = new ArrayList<Food>();
        pigeonList = new ArrayList<Pigeon>();
        ms = new Mouse(this);
        this.addMouseListener(ms);
    }

    public void addFood(int x, int y) {
        int size = foodList.size();
        foodList.add(new Food(x,y));
        //System.out.println("add food go pigeon");
        if (size == 0) {
            for (Pigeon pigeon : pigeonList
            ) {
                synchronized (pigeon) {
                    pigeon.notify();
                }
                //System.out.println("notify pigeon");
            }
        }
        this.repaint();
    }

    public void deleteFood(Food food) {
        //System.out.println("delete the food");
        foodList.remove(food);
    }

    public void addPigeon(Pigeon pigeon) {
        pigeonList.add(pigeon);
    }

    Food getFreshestFood() {
        Food freshestFood = null;
        if (!foodList.isEmpty()) {
            freshestFood = foodList.get(0);
            for (int i = 1; i < foodList.size(); i++) {
                Food testedFood = this.foodList.get(i);
                if (freshestFood.fresh > testedFood.fresh) {
                    freshestFood = testedFood;
                }
            }
        }

        return freshestFood;
    }

    float distanceBetweenPoint(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2));
    }

    public boolean hasNoFood() {
        return foodList.isEmpty();
    }


    public static void main(String[] args) {

        PigeonMap map = new PigeonMap(1200, 700);

        JFrame frame = new JFrame("PigeonMap");
        map.setSize(1200, 700);
        frame.add(map);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);



        for (int i = 0; i < Integer.parseInt(args[0]) ; i++) {
            (new Pigeon(map)).start();
        }

        while(true) {
            synchronized (map) {
                for (int i = 0; i < map.foodList.size(); i++) {
                    map.foodList.get(i).UpdateFresh();
                }
            }
        }

    }

    synchronized public void paint(Graphics g) {
       for (Pigeon pigeon : pigeonList
             ) {
            g.setColor(Color.GRAY);
            g.fillOval((int)pigeon.posX - rangePigeon/2, (int) pigeon.posY - rangePigeon/2, rangePigeon, rangePigeon);
        }

        for (Food food: foodList
             ) {
            g.setColor(Color.GREEN);
            g.fillRect((int)food.x - rangeFood/2, (int) food.y - rangeFood/2, rangeFood, rangeFood);
        }
    }
}
