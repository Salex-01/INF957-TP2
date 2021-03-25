import java.util.LinkedList;

public class Message {
    Node source;
    Node destination;
    LinkedList<Node> path = new LinkedList<>();
    int age = 0;
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

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void rewardPath() {
        for(Node n:path){
            synchronized (n.score){
                n.score++;
            }
        }
    }
}