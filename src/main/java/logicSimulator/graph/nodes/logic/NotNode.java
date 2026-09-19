package logicSimulator.graph.nodes.logic;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class NotNode extends Node {
    public NotNode(String name) {
        super(name);
        inputs.put("In", new Pin("In", this));
        outputs.put("Out", new Pin("Out", this));
    }

    @Override
    public void update(Graph graph) {
        Pin in = inputs.get("In");
        Pin out = outputs.get("Out");

        Pin.State targetState = (in.getState() == Pin.State.LOW) ? Pin.State.HIGH : Pin.State.LOW;

        graph.queueEvent(out, targetState, 1);
    }
}
