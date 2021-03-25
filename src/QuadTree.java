import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class QuadTree {
    int level;
    double x;
    double y;
    double w;
    double h;
    double dMax;
    QuadTree[] q;
    LinkedList<Node> nodes = new LinkedList<>();

    public QuadTree(int l, double x1, double y1, double w1, double h1, double d) {
        level = l;
        x = x1;
        y = y1;
        w = w1;
        h = h1;
        dMax = d;
    }

    public void add(Node n) {
        if (q == null) {
            nodes.add(n);
            if (nodes.size() > 100 && level > 0) {
                q = new QuadTree[4];
                q[0] = new QuadTree(level - 1, x, y, w / 2, h / 2, dMax);
                q[1] = new QuadTree(level - 1, x + w / 2, y, w / 2, h / 2, dMax);
                q[2] = new QuadTree(level - 1, x, y + h / 2, w / 2, h / 2, dMax);
                q[3] = new QuadTree(level - 1, x + w / 2, y + h / 2, w / 2, h / 2, dMax);
                while (nodes.size() > 0) {
                    addToSubLevel(nodes.remove(0));
                }
            } else {
                List<Node> acc = accessible(n);
                for (Node n1 : acc) {
                    n.connect(n1);
                }
            }
        } else {
            addToSubLevel(n);
        }
    }

    private void addToSubLevel(Node n) {
        if (n.x < x + w / 2) {
            if (n.y < y + h / 2) {
                q[0].add(n);
            } else {
                q[2].add(n);
            }
        } else {
            if (n.y < y + h / 2) {
                q[1].add(n);
            } else {
                q[3].add(n);
            }
        }
    }

    public List<Node> accessible(Node n) {
        return accessible(n, new LinkedList<>(), dMax);
    }

    private List<Node> accessible(Node n, List<Node> result, double d) {
        if (n.x < x - d || n.x > x + w + d || n.y < y - d || n.y > y + h + d) {
            return result;
        }
        if (q == null) {
            for (Node n1 : nodes) {
                if (n1.distance(n) <= d) {
                    result.add(n1);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                q[i].accessible(n, result, d);
            }
        }
        return result;
    }

    public boolean moveNode(Node n, double x1, double y1, boolean moveTo) {
        if (q == null) {
            remove(n);
            n.move(x1, y1, moveTo);
            if (n.x < x || n.x > x + w || n.y < y || n.y > y + h) {
                return true;
            } else {
                add(n);
                return false;
            }
        } else {
            QuadTree qt;
            if (n.x < x + w / 2) {
                if (n.y < y + h / 2) {
                    qt = q[0];
                } else {
                    qt = q[2];
                }
            } else {
                if (n.y < y + h / 2) {
                    qt = q[1];
                } else {
                    qt = q[3];
                }
            }
            if (qt.moveNode(n, x1, y1, moveTo)) {
                if (n.x < x || n.x > x + w || n.y < y || n.y > y + h) {
                    return true;
                } else {
                    add(n);
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private void remove(Node n) {
        n.disconnect();
        nodes.remove(n);
    }

    public QTIterator iterator() {
        return new QTIterator(this);
    }

    private static class QTIterator implements Iterator<Node> {
        QuadTree qt;
        QTIterator[] qti = new QTIterator[4];
        int i = 0;
        int j = 0;

        private QTIterator(QuadTree q) {
            qt = q;
        }

        @Override
        public boolean hasNext() {
            if (i < qt.nodes.size()) {
                return true;
            }
            if (qt.q != null) {
                initialize();
                while (!qti[j].hasNext() && j < 4) {
                    j++;
                }
                return (j < 4 && qti[j].hasNext());
            }
            return false;
        }

        @Override
        public Node next() {
            if (i < qt.nodes.size()) {
                Node n = qt.nodes.get(i);
                i++;
                return n;
            }
            if (qt.q != null) {
                initialize();
                while (!qti[j].hasNext() && j < 4) {
                    j++;
                }
                if (j >= 4 || !qti[j].hasNext()) {
                    throw new NoSuchElementException();
                }
                return qti[j].next();
            }
            throw new NoSuchElementException();
        }

        private void initialize() {
            if (qti[0] == null) {
                for (int k = 0; k < 4; k++) {
                    qti[k] = qt.q[k].iterator();
                }
            }
        }
    }
}