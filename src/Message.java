import java.util.LinkedList;

public class Message {
    Node source;
    Node destination;
    LinkedList<Node> path = new LinkedList<>();
    int age = 0;

    public Message(Node a, Node b) {
        source = a;
        destination = b;
    }

    public Message(Message m) {
        source = m.source;
        destination = m.destination;
    }
}
