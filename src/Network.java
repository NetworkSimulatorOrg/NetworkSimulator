import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
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

    public Node getNodeById(int index) {
        // Optimize for the case that nodes are saved in index order.
        if(nodes.get(index).getId() == index) {
            return nodes.get(index);
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getId() == index) {
                    return nodes.get(i);
                }
            }
        }
        System.out.println("No node found with id: " + index);
        return null;
    }

    private String[] parseLine(String line) {
        return line.split("[:, ][ ]*");
    }

    // Create a node of the proper type with id.
    private void buildNode(String[] line) {
        Node node;
        if(line[0].toUpperCase() == "CONNECT") {
            node = new ConnectNode(Integer.parseInt(line[1]), new ArrayList<Node>(), propagationDelay, distance);
            nodes.add(node);
        } else if(line[0].toUpperCase() == "COMPUTE") {
            node = new ComputeNode(Integer.parseInt(line[1]), new ArrayList<Node>(), propagationDelay, distance, msgProbability, msgLength, protocol);
            nodes.add(node);
        } else {
            System.out.println("Unrecognized node type: " + line[0]);
        }
    }

    // Add adjacent nodes to the node's adjacency list.
    private void addAdjacentNodes(String[] line) {
        Node node = this.getNodeById(Integer.parseInt(line[1]));

        for(int i = 2; i < line.length; i++) {
            node.addAdjacentNode(this.getNodeById(i));
        }
    }

    public void buildNodesFromFile(String file) {
        // https://www.journaldev.com/709/java-read-file-line-by-line
        var lines = new ArrayList<String[]>();
        try{
            var reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while(line != null) {
                lines.add(parseLine(line));
                line = reader.readLine();
            }
            reader.close();
        } catch(IOException e) {
            System.out.println(e);
        }

        for(var line : lines) {
            buildNode(line);
        }
        for(var line : lines) {
            addAdjacentNodes(line);
        }
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
