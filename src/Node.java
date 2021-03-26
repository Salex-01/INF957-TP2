import java.util.*;
import java.util.concurrent.Semaphore;

public class Node extends Thread {
    Graph g;
    double x;
    double y;
    int id;
    List<Node> connections = new ArrayList<>();
    Integer score = 0;
    final LinkedList<Message> messages = new LinkedList<>();
    int nNodes;
    final Semaphore s = new Semaphore(0);
    String transmissionMode;
    HashMap<Node, Pair<Node, Double>> routingTable;
    private final LinkedList<RouteMessage> routeMessages = new LinkedList<>();
    Semaphore routingSync;

    public Node(Graph g1, int ID, double x1, double y1, int nN, String tm) {
        g = g1;
        x = x1;
        y = y1;
        id = ID;
        nNodes = nN;
        transmissionMode = tm;
        if (tm.contentEquals("r")) {
            routingTable = new HashMap<>();
            routingSync = new Semaphore(0);
        }
    }

    public double distance(Node n) {
        return (x - n.x) * (x - n.x) + (y - n.y) * (y - n.y);
    }

    public void connect(Node... n1) {
        for (Node n : n1) {
            if (!connections.contains(n)) {
                connections.add(n);
                connections.sort((o1, o2) -> ((distance(o1) - distance(o2) < 0) ? -1 : ((distance(o1) - distance(o2) > 0) ? 1 : 0)));
                n.connections.add(this);
                n.connections.sort((o1, o2) -> ((n.distance(o1) - n.distance(o2) < 0) ? -1 : ((n.distance(o1) - n.distance(o2) > 0) ? 1 : 0)));
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

    public void addMessage(Message m, boolean isSource) {
        if (!isSource) {
            m.path.addLast(this);
        }
        synchronized (messages) {
            synchronized (s) {
                messages.addLast(m);
                s.release(1);
            }
        }
    }

    private boolean addRoute(RouteMessage rm) {
        final boolean[] tem = new boolean[1];
        Pair<Node, Double> route = routingTable.computeIfAbsent(rm.destination, node -> {
            tem[0] = true;
            return new Pair<>(rm.via, rm.distance);
        });
        if (route.value > rm.distance) {
            route.key = rm.via;
            route.value = rm.distance;
            return true;
        }
        return tem[0];
    }

    @Override
    @SuppressWarnings({"InfiniteLoopStatement", "SynchronizeOnNonFinalField", "BusyWait"})
    public void run() {
        if (transmissionMode.contentEquals("r")) {
            for (Node n : connections) {
                routeMessages.add(new RouteMessage(n, n, distance(n)));
            }
            while (routingTable.size() < nNodes - 1) {
                boolean tem;
                synchronized (routeMessages) {
                    tem = routeMessages.isEmpty();
                }
                if (tem) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    synchronized (routeMessages) {
                        tem = routeMessages.isEmpty();
                    }
                    while (!tem) {
                        RouteMessage rm;
                        synchronized (routeMessages) {
                            rm = routeMessages.removeFirst();
                        }
                        if (rm.destination != this) {
                            if (addRoute(rm)) {
                                for (Node n : connections) {
                                    synchronized (n.routeMessages) {
                                        n.routeMessages.addLast(new RouteMessage(rm.destination, this, rm.distance + distance(n)));
                                    }
                                }
                            }
                        }
                        synchronized (routeMessages) {
                            tem = routeMessages.isEmpty();
                        }
                    }
                }
            }
            synchronized (g.routing) {
                g.routing.release(1);
            }
            try {
                routingSync.acquire(1);
            } catch (InterruptedException ignored) {
            }
        }
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
            Message m;
            synchronized (messages) {
                m = messages.removeFirst();
            }
            if (m.destination == this) {
                m.rewardPath();
            } else {
                m.age++;
                if (m.age < nNodes) {
                    Node next = findNextNode(m);
                    if (next != null) {
                        next.addMessage(m, false);
                    } else {
                        System.out.println("Message " + m.id + " bloqué dans le noeud " + this.id);
                        synchronized (g.lostMessages) {
                            g.lostMessages++;
                        }
                    }
                } else {
                    System.out.println("Message " + m.id + " perdu dans le noeud " + this.id);
                    synchronized (g.lostMessages) {
                        g.lostMessages++;
                    }
                }
            }
        }
    }

    private Node findNextNode(Message m) {
        switch (transmissionMode) {
            case "pp":  // Noeud le plus proche
                return closest();
            case "ppnr":  // Noeud le plus proche sans demi-tour
                return closestNR((m.path.size() > 0 ? m.path.getLast() : null));
            case "ppu":  // Noeud le plus proche, passage unique
                return closestU(m.path);
            case "dd":   // Plus courte distance à la destination
                return distDest(m);
            case "ddnr":   // Plus courte distance à la destination sans demi-tour
                return distDestNR(m);
            case "ddu":   // Plus courte distance à la destination, passage unique
                return distDestU(m);
            case "r":   // Routage réseau
                return networkRouting(m);
            default:
                System.out.println("Mode de transmission inconnu : " + transmissionMode);
                System.exit(-1);
                break;
        }
        return null;
    }

    private Node closest() {
        return connections.get(0);
    }

    private Node closestNR(Node n0) {
        for (Node n : connections) {
            if (n != n0) {
                return n;
            }
        }
        return null;
    }

    private Node closestU(List<Node> l) {
        for (Node n : connections) {
            if (!l.contains(n)) {
                return n;
            }
        }
        return null;
    }

    private Node distDest(Message m) {
        Node next = connections.get(0);
        for (int i = 1; i < connections.size(); i++) {
            Node n = connections.get(i);
            if (m.destination.distance(n) < m.destination.distance(next)) {
                next = n;
            }
        }
        return next;
    }

    private Node distDestNR(Message m) {
        Node next = null;
        for (Node n : connections) {
            if (((m.path.size() == 0) || (m.path.getLast() != n))
                    && ((next == null) || ((m.destination.distance(n) < m.destination.distance(next))))) {
                next = n;
            }
        }
        return next;
    }

    private Node distDestU(Message m) {
        Node next = null;
        for (Node n : connections) {
            if ((!m.path.contains(n))
                    && (next == null || ((m.destination.distance(n) < m.destination.distance(next))))) {
                next = n;
            }
        }
        return next;
    }

    private Node networkRouting(Message m) {
        return routingTable.get(m.destination).key;
    }

    @Override
    public String toString() {
        return "Node " + id + ", score = " + score + ", position = " + x + "," + y;
    }

    private static class RouteMessage {
        Node destination;
        Node via;
        double distance;

        private RouteMessage(Node dest, Node v, double d) {
            destination = dest;
            via = v;
            distance = d;
        }
    }
}