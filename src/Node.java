import java.util.ArrayList;
import java.util.List;

public class Node {
    double x;
    double y;
    List<Node> connections = new ArrayList<>();

    public Node(double x1, double y1) {
        x = x1;
        y = y1;
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
}