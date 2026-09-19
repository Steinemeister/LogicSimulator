package logicSimulator.graph.nodes.io;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class LedNode extends Node {
    public LedNode(String name) {
        super(name);
        inputs.add(new Pin("In", this));
    }

    public boolean isOn() {
        return inputs.get(0).getState() == Pin.State.HIGH;
    }

    @Override
    public void update(Graph graph) {
        // Die LED reagiert passiv, sie muss selbst keine neuen Events erzeugen
    }
}
