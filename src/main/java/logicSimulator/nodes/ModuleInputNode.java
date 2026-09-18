package logicSimulator.nodes;

import logicSimulator.Pin;

public class ModuleInputNode extends Node {
    public ModuleInputNode(String name) {
        super(name);
        outputs.put("Out", new Pin("Out"));
    }

    @Override public void update() {}
}