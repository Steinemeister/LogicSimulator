package logicSimulator.graph.nodes.io;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class LedNode extends Node {
    public LedNode(String name) {
        super(name);
        inputs.put("In", new Pin("In", this));
    }

    public boolean isOn() {
        return inputs.get("In").getState() == Pin.State.HIGH;
    }

    @Override
    public void update(Graph graph) {
        // Die LED reagiert passiv, sie muss selbst keine neuen Events erzeugen
    }
}
