package Q1;

public class Food {

    public static final long MAX_FOOD_AGE = 30000;
    public double x;
    public double y;
    // moment de la creation de la nourriture
    public long creation = System.currentTimeMillis();

    Food(float x, float y) {
        this.x = x;
        this.y = y;
    }

}