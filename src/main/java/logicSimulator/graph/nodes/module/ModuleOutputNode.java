package logicSimulator.graph.nodes.module;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class ModuleOutputNode extends Node {
    public ModuleOutputNode(String name) {
        super(name);
        inputs.add(new Pin("In", this));

        calculateLayout(20f);
    }

    @Override public void update(Graph graph) {}
}