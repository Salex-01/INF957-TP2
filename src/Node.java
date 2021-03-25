import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Node extends Thread {
    double x;
    double y;
    int id;
    List<Node> connections = new ArrayList<>();
    Integer score = 0;
    final LinkedList<Message> messages = new LinkedList<>();
    final Semaphore s = new Semaphore(0);
    int nNodes;

    public Node(int ID, double x1, double y1, int nN) {
        x = x1;
        y = y1;
        id = ID;
        nNodes = nN;
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

    public void addMessage(Message m) {
        synchronized (messages) {
            synchronized (s) {
                messages.addLast(m);
                s.release(1);
            }
        }
    }

    @Override
    @SuppressWarnings({"InfiniteLoopStatement"})
    public void run() {
        boolean tem;
        while (true) {
            tem = false;
            synchronized (messages) {
                synchronized (s) {
                    if (messages.size() == 0) {
                        s.drainPermits();
                        tem = true;
                    }
                }
            }
            if (tem) {
                try {
                    s.acquire(1);
                } catch (InterruptedException ignored) {
                }
            }
            Message m = messages.removeFirst();
            if (m.destination == this) {
                m.rewardPath();
            } else {
                m.age++;
                if (m.age < nNodes) {
                    findNextNode(m).addMessage(m);
                } else {
                    System.out.println("Message " + m.id + " perdu dans le noeud " + this.id);
                }
            }
        }
    }

    private Node findNextNode(Message m) {

    }
}