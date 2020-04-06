import java.util.ArrayList;

public class Network {

    private int nodeCount, msgLength, distance;
    private double propagationDelay, msgProbability;
    private Protocol protocol;
    private ArrayList<Node> nodes;
    
    
    // Arguments: protocol
    public static void main(String[] args){
        Network network = new Network(null);
    }

    public Network(Protocol protocol){
        msgLength = 20;
        distance = 5;
        propagationDelay = 20;
        msgProbability = .3;
        this.protocol = protocol;
        buildNodes();
    }

    private void buildNodes(){
        // Hardcoding a node layout to test the protocols on

        int id = 0;
        
        for(; id < 5; id++){
            nodes.add(new ComputeNode(id, new ArrayList<Node>(), propagationDelay, distance, msgProbability, msgLength, protocol));
        }

        for(; id < 8; id++){
            nodes.add(new ConnectNode(id, new ArrayList<Node>(), propagationDelay, distance));
        }

        nodeCount = id;

        // Add the adjacent nodes
        nodes.get(0).addAdjacentNode(nodes.get(7));
        nodes.get(1).addAdjacentNode(nodes.get(7));
        nodes.get(2).addAdjacentNode(nodes.get(6));
        nodes.get(3).addAdjacentNode(nodes.get(5));
        nodes.get(4).addAdjacentNode(nodes.get(5));
        
        nodes.get(5).addAdjacentNode(nodes.get(3));
        nodes.get(5).addAdjacentNode(nodes.get(4));
        nodes.get(5).addAdjacentNode(nodes.get(6));

        nodes.get(6).addAdjacentNode(nodes.get(2));
        nodes.get(6).addAdjacentNode(nodes.get(5));
        nodes.get(6).addAdjacentNode(nodes.get(7));

        nodes.get(7).addAdjacentNode(nodes.get(0));
        nodes.get(7).addAdjacentNode(nodes.get(1));
        nodes.get(7).addAdjacentNode(nodes.get(6));
    }

}
