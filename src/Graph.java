import java.awt.*;
import java.util.List;
import java.util.*;

public class Graph extends Thread {
    int nN;
    double dMax;
    int nMessages;
    QuadTree nodesQT;
    Node[] nodes;
    Random r = new Random();
    Message lastSent = null;
    Canvas c;

    public Graph(int nNodes, double dmax, double s, Canvas ca, int nM) {
        c = ca;
        nMessages = nM;
        if (s > 0) {
            nodesQT = new QuadTree((int) (Math.log(nN) / Math.log(100)), 0, 0, s, s, dMax);
        } else {
            nodesQT = new QuadTree((int) (Math.log(nN) / Math.log(100)), 0, 0, Math.sqrt(nN), Math.sqrt(nN), dMax);
        }
        nN = nNodes;
        dMax = dmax;
        nodes = new Node[nN];
        Node n;
        for (int i = 0; i < nNodes; i++) {
            n = new Node(i + 1, r.nextDouble() % Math.sqrt(nNodes), r.nextDouble() % Math.sqrt(nNodes), nN);
            nodesQT.add(n);
            nodes[i] = n;
        }
        LinkedList<Node> e = evaluate();
        while (e.size() > 0) {
            for (Node n1 : e) {
                moveNode(n1, r.nextDouble() % Math.sqrt(nNodes), r.nextDouble() % Math.sqrt(nNodes), true);
            }
            e = evaluate();
        }
    }

    public void moveNode(Node n, double x1, double y1, boolean moveTo) {
        nodesQT.moveNode(n, x1, y1, moveTo);
    }

    private LinkedList<Node> evaluate() {
        List<Node> explored = new LinkedList<>();
        HashMap<Node, LinkedList<Node>> connected = new HashMap<>();
        Iterator<Node> i = nodesQT.iterator();
        LinkedList<Node> root = new LinkedList<>();
        while (i.hasNext()) {
            Node n1 = i.next();
            if (explored.contains(n1)) {
                continue;
            }
            explored.add(n1);
            LinkedList<Node> l = new LinkedList<>();
            connected.put(n1, l);
            l.add(n1);
            LinkedList<Node> toExplore = new LinkedList<>();
            toExplore.add(n1);
            while (toExplore.size() > 0) {
                Node n2 = toExplore.remove(0);
                for (Node n3 : n2.connections) {
                    if (!l.contains(n3)) {
                        l.add(n3);
                        toExplore.add(n3);
                        explored.add(n3);
                    }
                }
            }
        }
        Collection<LinkedList<Node>> c = connected.values();
        for (LinkedList<Node> l : c) {
            if (l.size() > root.size()) {
                root = l;
            }
        }
        c.remove(root);
        LinkedList<Node> toAdd = new LinkedList<>();
        for (LinkedList<Node> l : c) {
            while (l.size() > 0) {
                toAdd.add(l.remove(0));
            }
        }
        return toAdd;
    }

    public void sendMessage(Node a, Node b) {
        Message m = new Message(a, b);
        a.addMessage(m);
        lastSent = m;
    }

    public void sendMessage(Message m) {
        m.source.addMessage(m);
        lastSent = m;
    }

    public boolean repeatLast() {
        if (lastSent != null) {
            sendMessage(new Message(lastSent));
            return true;
        } else {
            return false;
        }
    }

    public Node getRandomNode() {
        return nodes[r.nextInt(nodes.length)];
    }

    public Node getNode(int i) {
        return nodes[i - 1];
    }

    private void draw() {
        Graphics g = c.getGraphics();
        int w = c.getWidth();
        int h = c.getHeight();
        g.clearRect(0, 0, c.getWidth(), c.getHeight());
        if (nodes.length > 1000) {
            g.setColor(Color.BLACK);
            g.drawString("Pas d'affichage lorsqu'il y a plus de 1000 noeuds", 10, 10);
            return;
        }
        Iterator<Node> i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            for (Node n1 : n.connections) {
                g.drawLine((int) (n.x / w), (int) (n.y / h), (int) (n1.x / w), (int) (n1.y / h));
            }
        }
        g.setColor(Color.GRAY);
        i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            g.fillOval((int) (n.x / w) - 15, (int) (n.y / h) - 15, 30, 30);
        }
        g.setColor(Color.BLACK);
        i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            g.drawString(String.valueOf(n.id), (int) (n.x / w) - 12, (int) (n.y / h) - 12);
            g.drawString(String.valueOf(n.score), (int) (n.x / w) - 12, (int) (n.y / h) + 2);
        }
    }

    @Override
    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public void run() {
        while (nMessages > 0) {
            Node a = getRandomNode();
            Node b;
            do {
                b = getRandomNode();
            } while (a == b);
            sendMessage(a, b);
            nMessages--;
        }
        for (Node n : nodes) {
            n.start();
        }
        while (true) {
            draw();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }
}