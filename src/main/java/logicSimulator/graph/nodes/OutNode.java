package logicSimulator.graph.nodes;

import logicSimulator.graph.Node;
import logicSimulator.graph.Pin;

import java.util.List;

public class OutNode extends Node {
    public OutNode(String nodeTypeName) {
        super(nodeTypeName);
    }

    @Override
    public List<Pin> update() {
        return List.of();
    }
}
