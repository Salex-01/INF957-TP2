import javax.swing.*;
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
    double displayW;
    double displayH;
    double minDX;
    double minDY;
    private boolean sendMessageContinuous = false;

    public Graph(int nN, double dmax, double s, Canvas ca, int nM, boolean fd, String tm) {
        nNodes = nN;
        dMax = dmax;
        size = (s > 0 ? s : Math.sqrt(nNodes));
        c = ca;
        nMessages = nM;
        forceDisplay = fd;
        nodesQT = new QuadTree((int) (Math.log(nNodes) / Math.log(100)), 0, 0, size, size, dMax);
        for (int i = 0; i < nN; i++) {
            Node n = new Node(this, i + 1, (r.nextDouble() * 0.8 + 0.1) * size, (r.nextDouble() * 0.8 + 0.1) * size, nNodes, tm);
            nodesQT.add(n);
            nodes.add(i, n);
        }
        LinkedList<Node> e = evaluate();
        while (e.size() > 0) {
            for (Node n1 : e) {
                moveNode(n1, (r.nextDouble() * 0.8 + 0.1) * size, (r.nextDouble() * 0.8 + 0.1) * size, true, false);
            }
            e = evaluate();
        }
        resizeDisplay();
        transmissionMode = tm;
        if (transmissionMode.contentEquals("r")) {
            routing = new Semaphore(0);
        }
    }

    private void resizeDisplay() {
        Iterator<Node> i = nodesQT.iterator();
        double minx = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = Double.MIN_VALUE;
        while (i.hasNext()) {
            Node n = i.next();
            if (n.x < minx) {
                minx = n.x;
            }
            if (n.x > maxx) {
                maxx = n.x;
            }
            if (n.y < miny) {
                miny = n.y;
            }
            if (n.y > maxy) {
                maxy = n.y;
            }
        }
        minDX = minx;
        minDY = miny;
        displayW = (maxx - minx) * 1.1;
        displayH = (maxy - miny) * 1.1;
    }

    public void moveNode(Node n, double x1, double y1, boolean moveTo, boolean resize) {
        nodesQT.moveNode(n, x1, y1, moveTo);
        if (resize) {
            resizeDisplay();
        }
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
                g.drawLine((int) (((n.x - minDX) * w) / displayW + 0.0455 * w), (int) (((n.y - minDY) * h) / displayH + 0.0455 * h), (int) (((n1.x - minDX) * w) / displayW + 0.0455 * w), (int) (((n1.y - minDY) * h) / displayH + 0.0455 * h));
            }
        }
        g.setColor(Color.GRAY);
        i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            g.fillOval((int) (((n.x - minDX) * w) / displayW + 0.0455 * w) - 15, (int) (((n.y - minDY) * h) / displayH + 0.0455 * h) - 15, 30, 30);
        }
        g.setColor(Color.BLACK);
        i = nodesQT.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            g.drawString(String.valueOf(n.id), (int) (((n.x - minDX) * w) / displayW + 0.0455 * w) - 4 * (Math.max(0, (int) Math.log10(n.id)) + 1), (int) (((n.y - minDY) * h) / displayH + 0.0455 * h) - 2);
            g.drawString(String.valueOf(n.score), (int) (((n.x - minDX) * w) / displayW + 0.0455 * w) - 4 * (Math.max(0, (int) Math.log10(n.score)) + 1), (int) (((n.y - minDY) * h) / displayH + 0.0455 * h) + 12);
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
            long start = System.currentTimeMillis();
            System.out.println("Initialisation des tables de routage");
            try {
                routing.acquire(nNodes);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Tables de routage initialisées en " + (System.currentTimeMillis() - start) + " ms");
            for (Node n : nodes) {
                n.routingSync.release(1);
            }
        }
        while (true) {
            long start = System.currentTimeMillis();
            draw();
            if (sendMessageContinuous) {
                for (int i = 0; i < 10000; i++) {
                    Node a = getRandomNode();
                    Node b;
                    do {
                        b = getRandomNode();
                    } while (a == b);
                    sendMessage(a, b);
                }
            }
            try {
                Thread.sleep(Math.max(1000 - (System.currentTimeMillis() - start), 100));
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void terminate() {
        nodes.sort((o1, o2) -> o2.score - o1.score);
        JOptionPane.showMessageDialog(null, "Gagnant : " + nodes.get(0) + "\nTaux de perte : " + ((lostMessages * 1.) / totalMessages) + " (" + lostMessages + " sur " + totalMessages + ")");
        System.out.println("Gagnant : " + nodes.get(0));
        System.out.println("Taux de perte : " + ((lostMessages * 1.) / totalMessages) + " (" + lostMessages + " sur " + totalMessages + ")");
    }

    public boolean toggleContinuous() {
        sendMessageContinuous = !sendMessageContinuous;
        return sendMessageContinuous;
    }

    public void stopContinuous() {
        sendMessageContinuous = false;
    }

    public void showLeaderboard() {
        nodes.sort((o1, o2) -> o2.score - o1.score);
        StringBuilder s = new StringBuilder();
        for (int i = 0; (i < 10 && i < nodes.size()); i++) {
            Node n = nodes.get(i);
            s.append(i + 1).append(": Node ").append(n.id).append(", score = ").append(n.score).append("\n");
        }
        JOptionPane.showMessageDialog(null, s, "Leaderboard", JOptionPane.PLAIN_MESSAGE);
    }
}