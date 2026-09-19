package logicSimulator.nodes;

import logicSimulator.Graph;
import logicSimulator.Pin;

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
