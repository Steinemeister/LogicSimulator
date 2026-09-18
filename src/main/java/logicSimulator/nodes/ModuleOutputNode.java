package logicSimulator.nodes;

import logicSimulator.Pin;

public class ModuleOutputNode extends Node {
    public ModuleOutputNode(String name) {
        super(name);
        inputs.put("In", new Pin("In"));
    }

    @Override public void update() {}
}