package logicSimulator.graph.nodes.logic;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class NotNode extends Node {
    public NotNode(String name) {
        super(name);
        inputs.add(new Pin("In", this));
        outputs.add(new Pin("Out", this));

        calculateLayout(24f);
    }

    @Override
    public void update(Graph graph) {
        Pin in = inputs.get(0);
        Pin out = outputs.get(0);

        Pin.State targetState = (in.getState() == Pin.State.LOW) ? Pin.State.HIGH : Pin.State.LOW;

        graph.queueEvent(out, targetState, 1);
    }
}
