package logicSimulator.graph.nodes.logic;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class AndNode extends Node {
    public AndNode(String name) {
        super(name);
        inputs.put("A", new Pin("A", this));
        inputs.put("B", new Pin("B", this));
        outputs.put("Out", new Pin("Out", this));
    }

    @Override
    public void update(Graph graph) {
        boolean a = inputs.get("A").getState() == Pin.State.HIGH;
        boolean b = inputs.get("B").getState() == Pin.State.HIGH;

        Pin.State result = (a && b) ? Pin.State.HIGH : Pin.State.LOW;
        graph.queueEvent(outputs.get("Out"), result, 1);
    }
}