import java.awt.*;

public class Main4 {
    int nNodes = 5;
    double dMax = 1;
    int nMessages = -1;
    String tMode = "pp";
    Graph g;

    public static void main(String[] args) {
        new Main4(args);
    }

    // [n/nodes <int>] [d/distance <double>] [m/messages <int>] [s/size <double>]
    // [tm/transmission <"pp","ppnr","ppu","dd","ddnr","ddu","r">] ["fd"/"forcedisplay"]
    Main4(String[] args) {
        double size = -1;
        boolean forceDisplay = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "nodes":
                case "n":
                    nNodes = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "distance":
                case "d":
                    dMax = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                case "messages":
                case "m":
                    nMessages = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "size":
                case "s":
                    size = Double.parseDouble(args[i + 1]);
                    i++;
                    break;
                case "transmission":
                case "tm":
                    tMode = args[i + 1];
                    i++;
                    break;
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
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Frame f = new Frame("Graph Mailer");
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
        System.out.println("Création du graph");
        long start = System.currentTimeMillis();
        g = new Graph(nNodes, dMax, size, new GraphCanvas(c), nMessages, forceDisplay, tMode);
        System.out.println("Création terminée en " + (System.currentTimeMillis() - start) + " ms");
        f.addWindowListener(new CloserListener(f, g));
        f.setVisible(true);
        g.start();
    }
}