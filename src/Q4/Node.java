package Q4;

import java.util.*;
import java.util.concurrent.Semaphore;

public class Node extends Thread {
    Graph g;
    double x;   // Position du noeud
    double y;   //
    int id;
    List<Node> connections = new ArrayList<>(); // Liste des noeuds accessibles depuis le noeud courant
    Integer score = 0;
    final LinkedList<Message> messages = new LinkedList<>();    // Messages en attente de traitement
    int nNodes;
    final Semaphore s = new Semaphore(0);   // Pour la gestion du réveil lorsqu'un message est reçu
    String routingAlgorithm;    // Algo de routage des messages
    HashMap<Node, Pair<Node, Double>> routingTable; // Table de routage du noeud si on utilise le routage réseau
    private final LinkedList<RoutingMessage> routingMessages = new LinkedList<>();  // Messages de routage en attente de traitement
    Semaphore routingSync;  // Pour la gestion du réveil après le remplissage des tables de routage
    boolean explored;   // Pour le parcours lors de la création du graph

    public Node(Graph g1, int ID, double x1, double y1, int nN, String ra) {
        g = g1;
        x = x1;
        y = y1;
        id = ID;
        nNodes = nN;
        routingAlgorithm = ra;
        // Si on utilise le routage réseau
        if (ra.contentEquals("r")) {
            routingTable = new HashMap<>();
            routingSync = new Semaphore(0);
        }
    }

    public double distance(Node n) {
        return Math.sqrt((x - n.x) * (x - n.x) + (y - n.y) * (y - n.y));
    }

    // Connexion du noeud courant à tous les noeuds passés en paramètre
    // et tri des connexions de la plus courte à la plus longue
    public void connect(Iterable<Node> nodes) {
        for (Node n : nodes) {
            if (!connections.contains(n)) {
                connections.add(n);
                connections.sort((o1, o2) -> ((distance(o1) - distance(o2) < 0) ? -1 : ((distance(o1) - distance(o2) > 0) ? 1 : 0)));
                n.connections.add(this);
                n.connections.sort((o1, o2) -> ((n.distance(o1) - n.distance(o2) < 0) ? -1 : ((n.distance(o1) - n.distance(o2) > 0) ? 1 : 0)));
            }
        }
    }

    // Coupure de toutes les connexions du noeud courant
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

    // Ajout d'un message dans la boite de réception
    public void addMessage(Message m, boolean isSource) {
        if (!isSource) {
            m.path.addLast(this);
        }
        synchronized (messages) {
            synchronized (s) {
                messages.addLast(m);
                // Pour réveiller le noeud si il dort
                s.release(1);
            }
        }
    }

    // Traitement d'une proposition de nouvelle route
    private boolean addRoute(RoutingMessage rm) {
        Pair<Node, Double> route = routingTable.get(rm.destination);
        if (route == null) {
            route = new Pair<>(rm.via, rm.distance);
            routingTable.put(rm.destination, route);
            return true;
        }
        if (rm.distance < route.value) {
            route.key = rm.via;
            route.value = rm.distance;
            return true;
        }
        return false;
    }

    // Traitement de tous les messages de routage reçus
    private void processNewRoutes() {
        boolean tem;
        synchronized (routingMessages) {
            tem = routingMessages.isEmpty();
        }
        // Tant qu'il y a des messages à traiter
        while (!tem) {
            RoutingMessage rm;
            synchronized (routingMessages) {
                try {
                    // Récupération du message
                    rm = routingMessages.removeFirst();
                } catch (NoSuchElementException e) {
                    // Pas censé arriver
                    return;
                }
            }
            if (rm.destination != this) {
                // Si la route est intéressante
                if (addRoute(rm)) {
                    // Notification à tous les noeuds accessibles que ce noeud propose une nouvelle route
                    for (Node n : connections) {
                        synchronized (n.routingMessages) {
                            n.routingMessages.addLast(new RoutingMessage(rm.destination, this, rm.distance + distance(n)));
                        }
                    }
                }
            }
            synchronized (routingMessages) {
                tem = routingMessages.isEmpty();
            }
        }
    }

    @Override
    @SuppressWarnings({"InfiniteLoopStatement", "SynchronizeOnNonFinalField", "BusyWait"})
    public void run() {
        boolean tem;
        // Si on utilise le routage réseau
        if (routingAlgorithm.contentEquals("r")) {
            // Ajout des routes vers tous les noeuds accessibles en un pas
            for (Node n : connections) {
                routingMessages.add(new RoutingMessage(n, n, distance(n)));
            }
            // Tant que la table de routage ne contient pas une route (pas forcément optimale) vers chaque noeud du graphe
            while (routingTable.size() < nNodes - 1) {
                synchronized (routingMessages) {
                    tem = routingMessages.isEmpty();
                }
                // Si aucun message reçu
                if (tem) {
                    try {
                        Thread.sleep(0,100000); // Dormir 100 µs
                    } catch (InterruptedException ignored) {
                    }
                } else {    // Sinon
                    processNewRoutes(); // Traitement des messages de routage reçus
                }
            }
            // Signaler que l'initialisation de la table de routage est terminée
            synchronized (g.routing) {
                g.routing.release(1);
            }
            // Dormir jusqu'à ce que tous les noeuds aient fini d'initialiser leurs tables de routage
            try {
                routingSync.acquire(1);
            } catch (InterruptedException ignored) {
            }
        }
        while (true) {
            tem = false;
            synchronized (messages) {
                synchronized (s) {
                    // Si on n'a plus de messages à traiter
                    if (messages.size() == 0) {
                        s.drainPermits();
                        tem = true;
                    }
                }
            }
            // Si on n'a plus de messages à traiter
            if (tem) {
                try {
                    // Attente jusqu'à avoir un message à traiter
                    s.acquire(1);
                } catch (InterruptedException ignored) {
                }
            }
            // Traitement des éventuelles propositions de nouvelles routes
            // Si le graph transmet un nombre arbitraire de messages,
            // les tables de routage de tous les noeuds deviennent optimales
            processNewRoutes();
            // Traitement des messages
            Message m;
            synchronized (messages) {
                m = messages.removeFirst();
            }
            // Si le message est arrivé à destination, augmenter les scores des noeuds qu'il a traversés
            if (m.destination == this) {
                m.rewardPath();
            } else {
                m.age++;
                // Si le message n'est pas trop vieux (perdu dans un trou noir)
                if (m.age < nNodes) {
                    // Détermination du noeud auquel l'envoyer ensuite
                    Node next = findNextNode(m);
                    // Si on a trouvé un noeud auquel l'envoyer
                    if (next != null) {
                        next.addMessage(m, false);
                    } else {    // Sinon, le message est bloqué
                        System.out.println("Message " + m.id + " bloqué dans le noeud " + this.id);
                        synchronized (g.lostMessages) {
                            g.lostMessages++;
                        }
                    }
                } else {    // Sinon, le message est bloqué
                    System.out.println("Message " + m.id + " perdu dans le noeud " + this.id);
                    synchronized (g.lostMessages) {
                        g.lostMessages++;
                    }
                }
            }
        }
    }

    // Détermination du noeud auquel transmettre le message, en fonction de l'algo de routage utilisé
    private Node findNextNode(Message m) {
        switch (routingAlgorithm) {
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
                System.out.println("Algorithme de routage inconnu : " + routingAlgorithm);
                System.exit(-1);
                break;
        }
        return null;
    }

    // Noeud le plus proche
    private Node closest() {
        return connections.get(0);  // Les connexions sont triées par longueur croissante
    }

    // Noeud le plus proche, hors celui spécifié (celui duquel le message a été reçu)
    private Node closestNR(Node n0) {
        for (Node n : connections) {
            if (n != n0) {
                return n;
            }
        }
        return null;
    }

    // Noeud le plus proche n'étant pas dans l (liste des noeuds déjà traversés par le message)
    private Node closestU(List<Node> l) {
        for (Node n : connections) {
            if (!l.contains(n)) {
                return n;
            }
        }
        return null;
    }

    // Noeud accessible le plus proche de la destination
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

    // Noeud accessible le plus proche de la destination, autre que celui duquel a été reçu le message
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

    // Noeud accessible le plus proche de la destination n'ayant jamais été traversé par m
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

    // Noeud accessible proposant le plus court chemin vers la destination
    private Node networkRouting(Message m) {
        return routingTable.get(m.destination).key;
    }

    @Override
    public String toString() {
        return "Node " + id + ", score = " + score + ", position = " + x + "," + y;
    }

    private static class RoutingMessage {
        Node destination;
        Node via;
        double distance;

        private RoutingMessage(Node dest, Node v, double d) {
            destination = dest;
            via = v;
            distance = d;
        }

        @Override
        public String toString() {
            return destination.id + " via " + via.id + " en " + distance;
        }
    }
}