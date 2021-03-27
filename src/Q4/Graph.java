package Q4;

import common.Terminable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Graph extends Thread implements Terminable {
    int nNodes; // Nombre de noeuds
    double dMax;    // Distance max pour que 2 noeuds soient connectés
    int nMessages;  // Nombre de messages à envoyer dès le lancement
    QuadTree nodesQT;   // Quadtree des noeuds
    final List<Node> nodes = new ArrayList<>();   // Liste des noeuds (plus rapide à parcourir)
    Random r = new Random();
    Message lastSent = null;    // Pour la répétition
    Canvas c;   // Zone d'affichage
    int totalMessages = 0;  // Nombre de messages envoyés
    Integer lostMessages = 0;   // Nombre de messages perdus en route
    Semaphore routing;  // Pour la synchro du remplissage des tables de routages (si applicable)
    String routingAlgo;    // Algo de transmission utilisé
    double size;    // Taille du graph
    boolean forceDisplay;   // Pour forcer l'affichage même sur un grand graph
    double displayW;    //
    double displayH;    // Paramètres pour l'affichage
    double minDX;       //
    double minDY;       //
    private boolean sendMessageContinuous = false;  // Pour l'envoi de messages en continu

    public Graph(int nN, double dmax, double s, Canvas ca, int nM, boolean fd, String rAlgo) {
        nNodes = nN;
        dMax = dmax;
        size = (s > 0 ? s : Math.sqrt(nNodes)); // Taille demandée ou par défaut
        c = ca;
        nMessages = nM;
        forceDisplay = fd;
        nodesQT = new QuadTree(0, 0, size, size, dMax);
        // Création de noeuds placés aléatoirement et insertion dans le quadtree
        for (int i = 0; i < nN; i++) {
            Node n = new Node(this, i + 1, (r.nextDouble() * 0.8 + 0.1) * size, (r.nextDouble() * 0.8 + 0.1) * size, nNodes, rAlgo);
            nodesQT.add(n);
            nodes.add(i, n);
        }
        // Évaluation de la connexité du graph (connexe -> e == null)
        LinkedList<Node> e = evaluate();
        // Tant que le graph n'est pas connexe
        while (e.size() > 0) {
            // Replacement aléatoire des noeuds qui ne font pas partie du plus grand ensemble connexe
            for (Node n1 : e) {
                moveNode(n1, (r.nextDouble() * 0.8 + 0.1) * size, (r.nextDouble() * 0.8 + 0.1) * size, true, false);
            }
            // Évaluation de la connexité du graph (connexe -> e == null)
            e = evaluate();
        }
        // Définition des paramètres d'affichage
        resizeDisplay();
        routingAlgo = rAlgo;
        // Si les messages sont orientés par l'algo de routage réseau
        if (routingAlgo.contentEquals("r")) {
            routing = new Semaphore(0);
        }
    }

    // Détermine les dimensions de la zone à afficher
    private void resizeDisplay() {
        double minx = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = Double.MIN_VALUE;
        for (Node n : nodes) {
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

    // Déplacement d'un noeud
    // moveTo = true : déplace le noeud aux coordonnées,
    // moveTo = false : déplace le noeud du vecteur spécifié
    public void moveNode(Node n, double x1, double y1, boolean moveTo, boolean resize) {
        nodesQT.moveNode(n, x1, y1, moveTo);
        if (resize) {
            resizeDisplay();
        }
    }

    // Retourne la liste des noeuds ne faisant pas partie du plus grand ensemble connexe
    private LinkedList<Node> evaluate() {
        // Map des ensembles connexes (référencés par le premier noeud de l'ensemble)
        HashMap<Node, LinkedList<Node>> connected = new HashMap<>();
        // Marquage de tous les noeuds comme non explorés
        for (Node n : nodes) {
            n.explored = false;
        }
        Iterator<Node> i = nodesQT.iterator();
        LinkedList<Node> root = new LinkedList<>(); // Plus grand ensemble connexe
        while (i.hasNext()) {
            Node n1 = i.next();
            // Si on a déjà exploré le noeud
            if (n1.explored) {
                continue;
            }
            // Marquage du noeud comme exploré
            n1.explored = true;
            // Création de la liste représentant son ensemble connexe
            LinkedList<Node> set = new LinkedList<>();
            connected.put(n1, set);
            set.add(n1);
            // Ajout à la liste des noeuds à explorer
            LinkedList<Node> toExplore = new LinkedList<>();
            toExplore.add(n1);
            // Tant qu'il y a des noeuds accessibles à explorer
            while (toExplore.size() > 0) {
                Node n2 = toExplore.remove(0);
                // Pour chaque noeud accessible à partir du noeud en cours d'exploration
                for (Node n3 : n2.connections) {
                    // Si il n'est pas encore exploré
                    if (!n3.explored) {
                        n3.explored = true;
                        set.add(n3);
                        toExplore.add(n3);
                    }
                }
            }
        }
        // Recherche du plus grand ensemble connexe
        Collection<LinkedList<Node>> c = connected.values();
        for (LinkedList<Node> set : c) {
            if (set.size() > root.size()) {
                root = set;
            }
        }
        c.remove(root);
        // Construction de la liste des noeuds ne faisant pas partie du plus grand ensemble connexe
        LinkedList<Node> toAdd = new LinkedList<>();
        for (LinkedList<Node> set : c) {
            while (set.size() > 0) {
                toAdd.add(set.remove(0));
            }
        }
        return toAdd;
    }

    // Envoi d'un message de a à b
    public void sendMessage(Node a, Node b) {
        totalMessages++;
        Message m = new Message(a, b);
        a.addMessage(m, true);
        lastSent = m;
    }

    // Envoi de m
    public void sendMessage(Message m) {
        totalMessages++;
        m.source.addMessage(m, true);
        lastSent = m;
    }

    // Envoi d'un message ayant la même source et la même destination que le dernier envoyé
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

    // Affichage du graph
    private void draw() {
        Graphics g = c.getGraphics();
        int w = c.getWidth();
        int h = c.getHeight();
        g.clearRect(0, 0, w, h);
        // L'affichage devient trop lourd au delà de 200 noeuds
        // Il peut quand même être forcé avec le paramètre "fd" au lancement du programme
        if (nodes.size() > 200 && !forceDisplay) {
            g.setColor(Color.BLACK);
            g.drawString("Pas d'affichage lorsqu'il y a plus de 200 noeuds", 10, 20);
            return;
        }
        // Liste des coordonnées d'affichage des noeuds
        HashMap<Node, Pair<Integer, Integer>> displayCoordinates = new HashMap<>();
        // Traçage des connexions entre les noeuds et calcul à la volée des coordonnées des d'affichage des noeuds
        for (Node n : nodes) {
            Pair<Integer, Integer> p = displayCoordinates.computeIfAbsent(n, node -> new Pair<>((int) (((n.x - minDX) * w) / displayW + 0.0455 * w), (int) (((n.y - minDY) * h) / displayH + 0.0455 * h)));
            for (Node n1 : n.connections) {
                Pair<Integer, Integer> p1 = displayCoordinates.computeIfAbsent(n1, node -> new Pair<>((int) (((n1.x - minDX) * w) / displayW + 0.0455 * w), (int) (((n1.y - minDY) * h) / displayH + 0.0455 * h)));
                g.drawLine(p.key, p.value, p1.key, p1.value);
            }
        }
        // Affichage des neouds
        g.setColor(Color.GRAY);
        for (Node n : nodes) {
            Pair<Integer, Integer> p = displayCoordinates.get(n);
            g.fillOval(p.key - 15, p.value - 15, 30, 30);
        }
        // Affichage des IDs et scores
        g.setColor(Color.BLACK);
        for (Node n : nodes) {
            Pair<Integer, Integer> p = displayCoordinates.get(n);
            g.drawString(String.valueOf(n.id), p.key - 4 * (Math.max(0, (int) Math.log10(n.id)) + 1), p.value - 2);
            g.drawString(String.valueOf(n.score), p.key - 4 * (Math.max(0, (int) Math.log10(n.score)) + 1), p.value + 12);
        }
    }

    @Override
    @SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
    public void run() {
        // Envoi du nombre de messages demandé au lancement (si applicable)
        while (nMessages > 0) {
            Node a = getRandomNode();
            Node b;
            do {
                b = getRandomNode();
            } while (a == b);
            sendMessage(a, b);
            nMessages--;
        }
        // Démarrage des noeuds
        for (Node n : nodes) {
            n.start();
        }
        // Si on utilise le routage réseau
        if (routingAlgo.contentEquals("r")) {
            long start = System.currentTimeMillis();
            System.out.println("Initialisation des tables de routage");
            // Attente de la fin de l'initialisation des tables de routage
            try {
                routing.acquire(nNodes);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Tables de routage initialisées en " + (System.currentTimeMillis() - start) + " ms");
            // Réveil des noeuds
            for (Node n : nodes) {
                n.routingSync.release(1);
            }
        }
        while (true) {
            long start = System.currentTimeMillis();
            // Affichage du graph
            draw();
            // Si l'envoi en continu est activé
            if (sendMessageContinuous) {
                // Envoi de messages 10000 par 10000 entre des noeuds choisis aléatoirement
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
        // Arrêt de l'envoi en continu
        stopContinuous();
        // Attente de l'arrivée des derniers messages
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        // Récupération du gagnant
        Node n = getBest();
        // Affichage des résultats
        JOptionPane.showMessageDialog(null, "Gagnant : " + n + "\nTaux de perte : " + ((lostMessages * 1.) / totalMessages) + " (" + lostMessages + " sur " + totalMessages + ")");
        System.out.println("Gagnant : " + n);
        System.out.println("Taux de perte : " + ((lostMessages * 1.) / totalMessages) + " (" + lostMessages + " sur " + totalMessages + ")");
    }

    // Retourne le noeud avec le meilleur score
    private Node getBest() {
        Node best = nodes.get(0);
        synchronized (nodes) {
            for (Node n : nodes) {
                if (n.score > best.score) {
                    best = n;
                }
            }
        }
        return best;
    }

    // Active/désactive l'envoi en continu
    public boolean toggleContinuous() {
        sendMessageContinuous = !sendMessageContinuous;
        return sendMessageContinuous;
    }

    public void stopContinuous() {
        sendMessageContinuous = false;
    }

    // Affichage du tableau des scores
    public void showLeaderboard() {
        ArrayList<Node> nCopy = new ArrayList<>(nodes);
        nCopy.sort((o1, o2) -> o2.score - o1.score);
        StringBuilder s = new StringBuilder();
        for (int i = 0; (i < 10 && i < nCopy.size()); i++) {
            Node n = nCopy.get(i);
            s.append(i + 1).append(": Q4.Node ").append(n.id).append(", score = ").append(n.score).append("\n");
        }
        JOptionPane.showMessageDialog(null, s, "Leaderboard", JOptionPane.PLAIN_MESSAGE);
    }
}