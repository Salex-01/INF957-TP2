import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Node extends Thread {
    double x;
    double y;
    int id;
    List<Node> connections = new ArrayList<>();
    int score = 0;
    final LinkedList<Message> messages = new LinkedList<>();

    public Node(int ID, double x1, double y1) {
        x = x1;
        y = y1;
        id = ID;
    }

    public double distance(Node n) {
        return (x - n.x) * (x - n.x) + (y - n.y) * (y - n.y);
    }

    public void connect(Node... n1) {
        for (Node n : n1) {
            if (!connections.contains(n)) {
                connections.add(n);
                n.connections.add(this);
            }
        }
    }

    public void disconnect() {
        while (connections.size() > 0) {
            connections.remove(0).connections.remove(this);
        }
    }

    public void move(double x1, double y1, boolean moveTo) {
        if (moveTo) {
            x = x1;
            y = y1;
        } else {
            x += x1;
            y += y1;
        }
    }

    public void add(Message m) {
        synchronized (messages) {
            messages.addLast(m);
        }
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        while (true) {
            if (messages.size() > 0) {
                
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}