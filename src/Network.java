import java.util.ArrayList;
import java.util.List;

public class Network {

    private int nodeCount, msgLength, distance;
    private double propagationDelay, msgProbability;
    private Protocol protocol;
    private List<Node> nodes;
    public static CSVWriter writer;
    public static Network network;

    
    // Arguments: protocol
    public static void main(String[] args){
        writer = new CSVWriter("csvOutput.csv");
        writer.openFile();

        Protocol protocol = new SlottedAloha(20);
        network = new Network(protocol);
        network.buildNodes();

        writer.closeFile();
    }

    public Network(Protocol protocol){
        msgLength = 20;
        distance = 5;
        propagationDelay = 20;
        msgProbability = 1;
        this.protocol = protocol;
    }

    private void buildNodes(){
        // Hardcoding a node layout to test the protocols on

        int id = 0;
        nodes = new ArrayList<>();

        for(; id < 3; id++){
            nodes.add(new ConnectNode(id, new ArrayList<Node>(), propagationDelay, distance));
        }

        for(; id < 9; id++){
            List<Node> adjacent = new ArrayList<>();
            // 2 connected to node 0. 2 connected to node 1. 2 connected to node 2.
            adjacent.add(nodes.get((id-3)/2));
            nodes.add(new ComputeNode(id, adjacent, propagationDelay, distance, msgProbability, msgLength, protocol));
        }

        nodeCount = id;

        // Add the adjacent nodes to the Connect Nodes
        
        nodes.get(0).addAdjacentNode(nodes.get(1));
        nodes.get(0).addAdjacentNode(nodes.get(3));
        nodes.get(0).addAdjacentNode(nodes.get(4));

        nodes.get(1).addAdjacentNode(nodes.get(0));
        nodes.get(1).addAdjacentNode(nodes.get(2));
        nodes.get(1).addAdjacentNode(nodes.get(5));
        nodes.get(1).addAdjacentNode(nodes.get(6));

        nodes.get(2).addAdjacentNode(nodes.get(1));
        nodes.get(2).addAdjacentNode(nodes.get(7));
        nodes.get(2).addAdjacentNode(nodes.get(8));
    }

    public void sendReport(Report report){
        report.logReport(writer);

        if (report.getType() == ReportType.Successful){
            // The packet arrived successfully
            return;
        }

        // There was a collision
        // @TODO: Act on the report
        
    }

}
