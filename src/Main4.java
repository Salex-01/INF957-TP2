public class Main4 {
    int nNodes = 5;
    double dMax = 1;
    int nMessages = 10;
    Graph g;

    public static void main(String[] args) {
        new Main4(args).execute();
    }

    Main4(String[] args){
        for(int i = 0; i<args.length;i++){
            switch (args[i]){
                case "nodes":
                case "n":
                    nNodes = Integer.parseInt(args[i+1]);
                    i++;
                    break;
                case "distance":
                case "d":
                    dMax = Double.parseDouble(args[i+1]);
                    i++;
                    break;
                case "messages":
                case "m":
                    nMessages = Integer.parseInt(args[i+1]);
                    i++;
                    break;
                default:
                    System.out.println("Argument inconnu : "+args[i]);
                    System.exit(-1);
            }
        }
        g = new Graph(nNodes,dMax);
    }

    void execute(){

    }
}