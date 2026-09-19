package logicSimulator.graph.nodes.module;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class ModuleInputNode extends Node {
    public ModuleInputNode(String name) {
        super(name);
        outputs.put("Out", new Pin("Out", this));

        calculateLayout(24f);
    }

    @Override public void update(Graph graph) {}
}