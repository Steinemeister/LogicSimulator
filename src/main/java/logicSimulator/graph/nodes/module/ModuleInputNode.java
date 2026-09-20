package logicSimulator.graph.nodes.module;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class ModuleInputNode extends Node {
    public ModuleInputNode(String name) {
        super(name);
        outputs.add(new Pin("Out", this));

        calculateLayout(20f);
    }

    @Override public void update(Graph graph) {}
}