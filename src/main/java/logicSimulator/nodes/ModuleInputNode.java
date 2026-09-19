package logicSimulator.nodes;

import logicSimulator.Graph;
import logicSimulator.Pin;

public class ModuleInputNode extends Node {
    public ModuleInputNode(String name) {
        super(name);
        outputs.put("Out", new Pin("Out", this));
    }

    @Override public void update(Graph graph) {}
}