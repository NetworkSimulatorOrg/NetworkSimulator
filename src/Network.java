import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.*;

public class Network {

    public static int computeNodeCount = 0, nodeCount = 0, longestDistance = 0, propagationRate;
    private int msgLength, distance;
    private Protocol protocol;
    private List<Node> nodes;
    public static CSVWriter writer;
    public static Network network;

    
    // Arguments: protocol
    public static void main(String[] args){
        writer = new CSVWriter("csvOutput.csv");
        writer.openFile();

        network = new Network();
        network.buildNodesFromFile("simple-network.txt");

        Protocol protocol = new TDMA(network.nodes);
        network.setProtocol(protocol);

        network.run();

        try {
            sleep(1000 * 10);
        } catch(InterruptedException e) {
            System.out.println("main: interrupted");
        } finally {
            System.out.println("ENDING");
            writer.closeFile();
            network.stop();
        }
    }

    public Network() {
        msgLength = 20;
        distance = 5;
        propagationRate = 20;
        nodes = new ArrayList<>();
    }

    public Network(Protocol protocol){
        this();
        this.protocol = protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;

        for(var node : nodes) {
            if(node instanceof ComputeNode) {
                ((ComputeNode) node).setProtocol(protocol);
            }
        }
    }

    public Node getNodeByIdNumber(int idNumber) {
        return getNodeById(Integer.toString(idNumber));
    }

    public Node getNodeById(String id) {
        // Optimize for the case that nodes are saved in index order.
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(id)) {
                return nodes.get(i);
            }
        }
        System.out.println("No node found with id: " + id);
        return null;
    }

    private String[] parseLine(String line) {
        return line.split("[:, ][ ]*");
    }

    // Create a node of the proper type with id.
    private void buildNode(String[] line) {
        Node node;
        if(line[0].toUpperCase().equals("CONNECT")) {
            node = new ConnectNode(line[1], propagationRate, distance);
            nodes.add(node);
            nodeCount++;
        } else if(line[0].toUpperCase().equals("COMPUTE")) {
            node = new ComputeNode(line[1], propagationRate, distance, msgLength, protocol);
            nodes.add(node);
            computeNodeCount++;
            nodeCount++;
        } else {
            System.out.println("Unrecognized node type: " + line[0]);
        }
    }

    // Add adjacent nodes to the node's adjacency list.
    private void addAdjacentNodes(String[] line) {
        Node node = this.getNodeById(line[1]);

        for(int i = 2; i < line.length; i++) {
            node.addAdjacentNode(this.getNodeById(line[i]));
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
        for(Node node : nodes) {
            // Set the longest distance of each compute node
            if (node instanceof ComputeNode){
                ((ComputeNode)node).setLastSenderStructureSize(nodeCount);
                node.longestDistance = findLongestDistance((ComputeNode)node, node, node);
                if (node.longestDistance > longestDistance){
                    longestDistance = node.longestDistance;
                }

                System.out.println(node.id + ": " + node.longestDistance);
            }
        }
    }

    public void run() {
        protocol.run();
        for(var node : nodes) {
            node.run();
        }
    }

    public void stop() {
        protocol.terminateThreads();
        for(var node : nodes) {
            node.terminateThreads();
        }
    }

    public void sendReport(Report report){
        report.logReport(writer);
    }

    // Finds the longest distance to any node from the current node without going through the previous node.
    public int findLongestDistance(ComputeNode original, Node current, Node previous){
        // This does not allow for cycles

        // Set the original node's lastSenderStructure so that the current node's previous value is the previous node (Used in messages)
        original.lastSenderStructure[Integer.parseInt(current.getId())] = previous.getId();

        int max = 0;
        boolean isLeaf = true;

        for (Node adjacent : current.adjacent){
            // Don't go to the previous node
            if (!adjacent.getId().equals(previous.getId())){
                isLeaf = false;
                
                // Find the furthest distance to a node that has not yet been visited.
                int len = findLongestDistance(original, adjacent, current);
                if (max < len){
                    max = len;
                }
            }
        }

        if (isLeaf){
            return 0;
        }

        return distance + max;
    }

}
