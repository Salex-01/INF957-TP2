import java.util.*;

public class Graph {
    int nN;
    double dMax;
    QuadTree nodesQT = new QuadTree((int) (Math.log(nN) / Math.log(4)), 0, 0, Math.sqrt(nN), Math.sqrt(nN), dMax);

    public Graph(int nNodes, double dmax) {
        Random r = new Random();
        nN = nNodes;
        dMax = dmax;
        for (int i = 0; i < nNodes; i++) {
            nodesQT.add(new Node(r.nextDouble() % Math.sqrt(nNodes), r.nextDouble() % Math.sqrt(nNodes)));
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
}