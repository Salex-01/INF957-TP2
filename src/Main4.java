import java.awt.*;

public class Main4 {
    int nNodes = 5;
    double dMax = 1;
    int nMessages = -1;
    Graph g;

    public static void main(String[] args) throws InterruptedException {
        new Main4(args);
    }

    Main4(String[] args) {
        double s = -1;
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
                    s = Double.parseDouble(args[i + 1]);
                    i++;
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
        Frame f = new Frame();
        f.setBounds((int) (d.getWidth() * 0.1), (int) (d.getHeight() * 0.1), (int) (d.getWidth() * 0.8), (int) (d.getHeight() * 0.8));
        f.addWindowListener(new CloserListener(f));
        Container c = new Container();
        c.setBounds(0, 0, f.getWidth(), f.getHeight() - 30);
        f.add(c);
        System.out.println("Création du graph");
        if (nMessages <= 0) {
            new MessageButtonManual(c, this);
            new MessageButtonRepeat(c, this);
            new MessageButton1(c, this);
            new MessageButton10(c, this);
            new MessageButton100(c, this);
        }
        long start = System.currentTimeMillis();
        g = new Graph(nNodes, dMax, s, new GraphCanvas(c, this, nMessages <= 0), nMessages);
        System.out.println("Création terminée en " + (System.currentTimeMillis() - start) + " ms");
        g.start();
    }
}