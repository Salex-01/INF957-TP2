package Q4;

import common.CloserListener;

import java.awt.*;

public class Main4 {
    int nNodes = 10;
    double dMax = 1;
    int nMessages = 0;
    String routingAlgorithm = "pp";
    Graph g;

    public static void main(String[] args) {
        new Main4(args);
    }

    // [n/nodes <int>] [d/distance <double>] [m/messages <int>] [s/size <double>]
    // [a/algo <"pp","ppnr","ppu","dd","ddnr","ddu","r">] ["fd"/"forcedisplay"]
    //
    // pp : noeud le plus proche
    // ppnr : noeud le plus proche sans demi-tour
    // ppu : noeud le plus proche, passage unique
    // dd : noeud le plus proche de la destination
    // ddnr : noeud le plus proche de la destination sans demi-tour
    // ddu : noeud le plus proche de la destination, passage unique
    // r : routage réseau
    Main4(String[] args) {
        double size = -1;
        boolean forceDisplay = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                // Nombre de noeuds
                case "nodes":
                case "n":
                    nNodes = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                // Distance max pour que les noeuds soient connectés
                case "distance":
                case "d":
                    dMax = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                // Nombre de messages à envoyer dès le lancement
                case "messages":
                case "m":
                    nMessages = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                // Taille du graph
                case "size":
                case "s":
                    size = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                // Algo de transmission (liste des valeurs possibles dans les commentaires en en-tête)
                case "algo":
                case "a":
                    routingAlgorithm = args[i + 1];
                    i++;
                    break;
                // Pour forcer l'affichage quand il y a plus de 200 noeuds dans le graph
                case "forcedisplay":
                case "fd":
                    forceDisplay = true;
                    break;
                default:
                    System.out.println("Argument inconnu : " + args[i]);
                    System.exit(-1);
            }
        }
        if (nNodes < 2) {
            System.out.println("Le graph doit contenir au moins 2 noeuds");
            System.exit(-1);
        }
        // Création de la fenêtre
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Frame f = new Frame("Q4.Graph Mailer");
        f.setBounds((int) (d.getWidth() * 0.1), (int) (d.getHeight() * 0.1), (int) (d.getWidth() * 0.8), (int) (d.getHeight() * 0.8));
        Container c = new Container();
        c.setBounds(0, 0, f.getWidth() - 10, f.getHeight() - 30);
        f.add(c);
        f.addComponentListener(new ResizeListener(c, f));
        if (nMessages <= 0) {
            new MessageButtonManual(c, this);
            new MessageButtonRepeat(c, this);
            new MessageButtonMultiple(c, this, 1);
            new MessageButtonMultiple(c, this, 10);
            new MessageButtonMultiple(c, this, 100);
            new MessageButtonMultiple(c, this, 1000);
            new MessageButtonMultiple(c, this, 10000);
            new MessageButtonContinuous(c, this);
            new LeaderboardButton(c, this);
        }
        // Création du graph
        System.out.println("Création du graph");
        long start = System.currentTimeMillis();
        g = new Graph(nNodes, dMax, size, new GraphCanvas(c), nMessages, forceDisplay, routingAlgorithm);
        System.out.println("Création terminée en " + (System.currentTimeMillis() - start) + " ms");
        f.addWindowListener(new CloserListener(f, g));
        f.setVisible(true);
        // Démarrage
        g.start();
    }
}