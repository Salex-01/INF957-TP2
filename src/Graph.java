import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Graph extends Thread {
    int nNodes;
    double dMax;
    int nMessages;
    QuadTree nodesQT;
    List<Node> nodes = new ArrayList<>();
    Random r = new Random();
    Message lastSent = null;
    Canvas c;
    int totalMessages = 0;
    Integer lostMessages = 0;
    Semaphore routing;
    String transmissionMode;
    double size;
    boolean forceDisplay;

    public Graph(int nN, double dmax, double s, Canvas ca, int nM, boolean fd, String tm) {
        nNodes = nN;
        dMax = dmax;
        size = (s > 0 ? s : Math.sqrt(nNodes));
        c = ca;
        nMessages = nM;
        forceDisplay = fd;
        nodesQT = new QuadTree((int) (Math.log(nNodes) / Math.log(100)), 0, 0, size, size, dMax);
        for (int i = 0; i < nN; i++) {
            Node n = new Node(this, i + 1, r.nextDouble() * size, r.nextDouble() * size, nNodes, tm);
            nodesQT.add(n);
            nodes.add(i, n);
        }
        LinkedList<Node> e = evaluate();
        while (e.size() > 0) {
            for (Node n1 : e) {
                moveNode(n1, r.nextDouble() * size, r.nextDouble() * size, true);
            }
            e = evaluate();
        }
        transmissionMode = tm;
        if (transmissionMode.contentEquals("r")) {
            routing = new Semaphore(0);
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
        totalMessages++;
        Message m = new Message(a, b);
        a.addMessage(m, true);
        lastSent = m;
    }

    public void sendMessage(Message m) {
        totalMessages++;
        m.source.addMessage(m, true);
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
        return nodes.get(r.nextInt(nodes.size()));
    }

    public Node getNode(int i) {
        return nodes.get(i - 1);
    }

    private void draw() {
        Graphics g = c.getGraphics();
        int w = c.getWidth();
        int h = c.getHeight();
        g.clearRect(0, 0, w, h);
        if (nodes.size() > 200 && !forceDisplay) {
            g.setColor(Color.BLACK);
            g.drawString("Pas d'affichage lorsqu'il y a plus de 200 noeuds", 10, 20);
            return;
        }
        Iterator<Node> i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            for (Node n1 : n.connections) {
                g.drawLine((int) ((n.x * w) / size), (int) ((n.y * h) / size), (int) ((n1.x * w) / size), (int) ((n1.y * h) / size));
            }
        }
        g.setColor(Color.GRAY);
        i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            g.fillOval((int) ((n.x * w) / size) - 15, (int) ((n.y * h) / size) - 15, 30, 30);
        }
        g.setColor(Color.BLACK);
        i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            g.drawString(String.valueOf(n.score), (int) ((n.x * w) / size) - 12, (int) ((n.y * h) / size) + 5);
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
        if (transmissionMode.contentEquals("r")) {
            try {
                routing.acquire(nNodes);
            } catch (InterruptedException ignored) {
            }
            for (Node n : nodes) {
                n.routingSync.release(1);
            }
        }
        while (true) {
            draw();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void terminate() {
        nodes.sort((o1, o2) -> o2.score - o1.score);
        System.out.println("Gagnant : " + nodes.get(0));
        System.out.println("taux de perte : " + ((lostMessages * 1.) / totalMessages));
    }
}