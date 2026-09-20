package logicSimulator.graph.nodes.logic;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class AndNode extends Node {
    public AndNode(String name) {
        super(name);

        inputs.add(new Pin("A", this));
        inputs.add(new Pin("B", this));
        outputs.add(new Pin("Out", this));

        calculateLayout(20f);
    }

    @Override
    public void update(Graph graph) {
        boolean a = inputs.get(0).getState() == Pin.State.HIGH;
        boolean b = inputs.get(1).getState() == Pin.State.HIGH;

        Pin.State result = (a && b) ? Pin.State.HIGH : Pin.State.LOW;
        graph.queueEvent(outputs.get(0), result, 1);
    }
}