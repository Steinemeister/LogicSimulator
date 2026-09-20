package logicSimulator.graph.nodes.io;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class LedNode extends Node {
    public LedNode(String name) {
        super(name);

        // 1. Eingangs-Pin hinzufügen
        inputs.add(new Pin("In", this));

        calculateLayout(20f);
    }

    public boolean isOn() {
        return inputs.get(0).getState() == Pin.State.HIGH;
    }

    @Override
    public void update(Graph graph) {
        // Die LED reagiert passiv, sie muss selbst keine neuen Events erzeugen
    }
}
