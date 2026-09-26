package logicSimulator;


import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.Node;
import logicSimulator.graph.Pin;
import logicSimulator.graph.nodes.NotNode;
import logicSimulator.rendering.Renderer;

import static imgui.app.Application.launch;

public class Main {
    public static void main(String[] args) {
        Graph graph = new Graph();

        Node notNode1 = new NotNode();
        notNode1.setPos(1, 1);
        Node notNode2 = new NotNode();
        notNode2.setPos(4, 1);
        graph.addNode(notNode1);
        graph.addNode(notNode2);

        Edge edge1 = new Edge(notNode1.getOutputPins().getFirst());
        edge1.addTarget(notNode1.getInputPins().getFirst());
        graph.addEdge(edge1);


        graph.queueEvent(notNode1.getOutputPins().getFirst(), 0, Pin.PinState.HIGH);

        Renderer renderer = new Renderer(graph);
        launch(renderer);
    }
}
