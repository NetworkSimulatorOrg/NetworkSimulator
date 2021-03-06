import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.*;

public class Network {

    public static int computeNodeCount = 0, nodeCount = 0, longestDistance = 0, propagationRate;
    public static double msgGenerationProbability = 0.75;
    private int msgLength, distance;
    private Protocol protocol;
    private List<Node> nodes;
    public CSVWriter writer;
    public static Network network;
    public static boolean logToConsole;

    
    // Arguments: protocol
    public static void main(String[] args){
        // For serious data generation
        String[] protocols = {/*"aloha", "slottedaloha",*/ "csma_cd"/*, "tdma", "polling", "tokenpassing"*/};
        String[] networks = {/*"complex-network", */"middling-network", "simple-network"};
        double[] msgProbability = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
        logToConsole = false;

        for(var network : networks) {
            for(var protocol : protocols) {
                for(var probability: msgProbability) {
                    Network.msgGenerationProbability = probability;
                    runner(network + ".txt", "csv_" + network + "_" + protocol + "_" + probability + ".csv", protocol, 1000 * 30);
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
        // Else
//        logToConsole = false;
//        String protocol = "tokenpassing";
//        runner("complex-network.txt", "csv_complex-network_" + protocol + ".csv", protocol, 1000 * 10);
    }

    public static void runner(String adjacencyList, String outputFile, String networkProtocol, long timeout) {
        System.out.println("Running network: " + adjacencyList + " \ton protocol " + networkProtocol + " \tfor " + timeout + " ms \toutputted to " + outputFile);
        network = new Network(outputFile);
        network.buildNodesFromFile(adjacencyList);

        Protocol protocol;
        switch(networkProtocol.toUpperCase()) {
            case "ALOHA":
                protocol = new Aloha();
                break;
            case "SLOTTEDALOHA":
                protocol = new SlottedAloha();
                break;
            case "CSMA_CD":
                protocol = new CSMA_CD();
                break;
            case "POLLING":
                protocol = new Polling();
                break;
            case "TDMA":
                protocol = new TDMA(network.nodes);
                break;
            case "TOKENPASSING":
                protocol = new TokenPassing(network.nodes);
                break;
            default:
                System.out.println("Unrecognized Protocol Name: " + networkProtocol);
                return;
        }
        network.setProtocol(protocol);

        network.run();

        try {
            sleep(timeout);
        } catch(InterruptedException e) {
            System.out.println("main: interrupted");
        } finally {
            System.out.println("ENDING");
            network.stop();
        }
    }

    public Network() {
        Node.resetIdNumbers();
        nodeCount = 0;
        computeNodeCount = 0;
        longestDistance = 0;
        propagationRate = 20;
        msgLength = 20;
        distance = 5;
        nodes = new ArrayList<>();
        protocol = null;
        writer = null;
    }

    public Network(String outputFile) {
        this();
        writer = new CSVWriter(outputFile);
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
        if (nodes.get(idNumber).getIdNumber() != idNumber) {
            // Optimize for the case that nodes are saved in index order.
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getIdNumber() == idNumber) {
                    return nodes.get(i);
                }
            }
            System.out.println("No node found with idNumber: " + idNumber);
            return null;
        } else {
            return nodes.get(idNumber);
        }
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
            node = new ComputeNode(line[1], propagationRate, distance, msgLength, protocol, Network.msgGenerationProbability);
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
            }
        }
    }

    public void run() {
        writer.openFile();
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
        writer.closeFile();
    }

    public void sendReport(Report report){
        report.logReport(writer);
    }

    // Finds the longest distance to any node from the current node without going through the previous node.
    public int findLongestDistance(ComputeNode original, Node current, Node previous){
        // This does not allow for cycles

        // Set the original node's lastSenderStructure so that the current node's previous value is the previous node (Used in messages)
        original.lastSenderStructure[current.getIdNumber()] = previous.getId();

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
