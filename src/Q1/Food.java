package Q1;

public class Food {
    public float x;
    public float y;
    public int fresh;

    long timeSinceStart;
    long oldTimeSinceStart;
    long timer;
    static float maxTimerFresh = 5;

    Food(float x, float y) {
        this.x = x;
        this.y = y;
        fresh = 0;
        System.out.println(fresh);
        timeSinceStart = System.currentTimeMillis() /1000;
        oldTimeSinceStart = 0;
        timer = 0;
    }

    void UpdateFresh() {
        timeSinceStart = System.currentTimeMillis() / 1000;
        timer += timeSinceStart - oldTimeSinceStart;
        oldTimeSinceStart = timeSinceStart;
        if (timer > maxTimerFresh) {
            this.fresh ++;
            timer = 0;
            System.out.println(fresh);
        }
    }

}
