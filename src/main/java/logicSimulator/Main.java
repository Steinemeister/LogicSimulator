package logicSimulator;


import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.NotNode;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.addNode(new NotNode("NOT"));
        graph.runSimulation();
    }
}
