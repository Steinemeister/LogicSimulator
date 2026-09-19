package logicSimulator.nodes;

import logicSimulator.Graph;
import logicSimulator.Pin;

public class ModuleOutputNode extends Node {
    public ModuleOutputNode(String name) {
        super(name);
        inputs.put("In", new Pin("In", this));
    }

    @Override public void update(Graph graph) {}
}