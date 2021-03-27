package Q4;

import java.util.LinkedList;

public class Message {
    Node source;
    Node destination;
    LinkedList<Node> path = new LinkedList<>(); // Liste des noeuds traversés par le message
    int age = 0;    // Pour arrêter les messages bloqués dans un trou noir
    static long index = 0;
    long id = index++;

    public Message(Node a, Node b) {
        source = a;
        destination = b;
    }

    @SuppressWarnings("all")
    public Message(Message m) {
        source = m.source;
        destination = m.destination;
    }

    // Augmentation du score de tous les noeuds traversés
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void rewardPath() {
        path.removeLast();
        for(Node n:path){
            synchronized (n.score){
                n.score++;
            }
        }
    }
}