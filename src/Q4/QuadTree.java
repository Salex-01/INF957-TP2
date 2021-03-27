package Q4;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class QuadTree {
    double x;   //
    double y;   // Position et taille
    double w;   //
    double h;   //
    double dMax;    // Distance maximale pour que 2 noeuds soient connectés
    QuadTree[] q;   // Niveau inférieur
    LinkedList<Node> nodes = new LinkedList<>();    // Noeuds contenus par ce niveau

    public QuadTree(double x1, double y1, double w1, double h1, double d) {
        x = x1;
        y = y1;
        w = w1;
        h = h1;
        dMax = d;
    }

    public void add(Node n) {
        // Si ce quadtree n'a pas de niveau inférieur
        if (q == null) {
            nodes.add(n);
            // Si il y a plus de 100 noeuds dans ce quadtree
            if (nodes.size() > 100) {
                // Création du niveau inférieur
                q = new QuadTree[4];
                q[0] = new QuadTree(x, y, w / 2, h / 2, dMax);
                q[1] = new QuadTree(x + w / 2, y, w / 2, h / 2, dMax);
                q[2] = new QuadTree(x, y + h / 2, w / 2, h / 2, dMax);
                q[3] = new QuadTree(x + w / 2, y + h / 2, w / 2, h / 2, dMax);
                // Transfert des noeuds de ce niveau vers le niveau inférieur
                while (nodes.size() > 0) {
                    addToSubLevel(nodes.remove(0));
                }
            } else {    // Sinon
                // Connexion du nouveau noeud à tous les noeuds accessibles
                n.connect(accessible(n));
            }
        } else {    // Sinon
            addToSubLevel(n);   // Ajout du noeud au niveau inférieur
        }
    }

    // Ajoute le noeud dans le bon quadtree du niveau inférieur
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

    // Liste des noeuds assez proche de n pour s'y connecter
    public List<Node> accessible(Node n) {
        return accessible(n, new LinkedList<>(), dMax);
    }

    private List<Node> accessible(Node n, List<Node> result, double d) {
        // Si le noeud est trop loin de ce quadtree pour se connecter à ses noeuds
        if (n.x < x - d || n.x > x + w + d || n.y < y - d || n.y > y + h + d) {
            return result;
        }
        // Si ce quadtree n'a pas de niveau inférieur
        if (q == null) {
            // Ajout à la liste de retour de tous les noeuds assez proches de n pour s'y connecter
            for (Node n1 : nodes) {
                if (n1 != n && n1.distance(n) <= d) {
                    result.add(n1);
                }
            }
        } else {    // Sinon
            // Appel récursif sur le niveau inférieur
            for (int i = 0; i < 4; i++) {
                q[i].accessible(n, result, d);
            }
        }
        return result;
    }

    // Déplace un noeud
    public boolean moveNode(Node n, double x1, double y1, boolean moveTo) {
        // Si ce quadtree n'a pas de niveau inférieur
        if (q == null) {
            remove(n);  // On le retire du graph
            n.move(x1, y1, moveTo); // On le déplace
            // Si le déplacement l'a fait sortir de ce quadtree
            if (n.x < x || n.x > x + w || n.y < y || n.y > y + h) {
                return true;
            } else {    // Sinon
                add(n); // On le remet à sa place dans ce quadtree
                return false;
            }
        } else {    // Sinon
            // On détermine dans quel quadtree du niveau inférieur il se trouve
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
            // Appel récursif sur le niveau inférieur
            // Si le déplacement a fait sortir le noeud du quadtree de niveau inférieur
            if (qt.moveNode(n, x1, y1, moveTo)) {
                // Si le déplacement a fait sortir le noeud de ce quadtree
                if (n.x < x || n.x > x + w || n.y < y || n.y > y + h) {
                    return true;
                } else {    // Sinon
                    add(n); // On le remet à sa place dans ce quadtree
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    // Retire un noeud du graph et de ce quadtree
    private void remove(Node n) {
        n.disconnect();
        nodes.remove(n);
    }

    public Iterator<Node> iterator() {
        return new QTIterator(this);
    }

    // Iterator de QuadTree
    private static class QTIterator implements Iterator<Node> {
        QuadTree qt;
        Iterator<Node>[] qti = new QTIterator[4];
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
                while (j < 4 && !qti[j].hasNext()) {
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
                while (j < 4 && !qti[j].hasNext()) {
                    j++;
                }
                if (j >= 4) {
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