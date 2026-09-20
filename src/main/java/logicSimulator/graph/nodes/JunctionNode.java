package logicSimulator.graph.nodes;

import logicSimulator.graph.Graph;

public class JunctionNode extends Node {
    public JunctionNode(String name) {
        super("JUNCTION");
        this.width = 0f;
        this.height = 0f;

        Pin universalPin = new Pin("Point", this);
        universalPin.setRelativePosition(0f, 0f);

        inputs.add(universalPin);
        outputs.add(universalPin);
    }

    @Override
    public void update(Graph graph) {
//        Pin pointPin = inputs.get(0);
//
//        graph.queueEvent(pointPin, pointPin.getState(), 0);
    }
}