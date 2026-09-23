package logicSimulator;


import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.Node;
import logicSimulator.graph.Pin;
import logicSimulator.graph.nodes.NotNode;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph();

        Node notNode1 = new NotNode("NOT");
        Node notNode2 = new NotNode("NOT");
        graph.addNode(notNode1);
        graph.addNode(notNode2);

        Edge edge1 = new Edge(notNode1.getOutputPins().getFirst());
        edge1.addTarget(notNode2.getInputPins().getFirst());
        graph.addEdge(edge1);

        Edge edge2 = new Edge(notNode2.getOutputPins().getFirst());
        edge1.addTarget(notNode1.getInputPins().getFirst());
        graph.addEdge(edge2);

        graph.queueEvent(notNode1.getOutputPins().getFirst(), 0, Pin.PinState.HIGH);


        Pin.PinState lastState = Pin.PinState.LOW;
        for (int i = 0; i < 2000; i++) {
            graph.step();
            if (notNode1.getOutputPins().getFirst().getState() == lastState) {
                System.out.println("funktioniert nicht");
                break;
            }
            lastState = notNode1.getOutputPins().getFirst().getState();
        }
    }
}
