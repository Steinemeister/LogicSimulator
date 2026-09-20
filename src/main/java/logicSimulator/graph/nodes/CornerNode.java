package logicSimulator.graph.nodes;

import logicSimulator.graph.Graph;

public class CornerNode extends Node {
    public CornerNode(String name) {
        super("CORNER");
        this.width = 0f;
        this.height = 0f;

        // Da der Knoten 0x0 groß ist, liegen beide Pins exakt im Zentrum bei (0,0)
        inputs.add(new Pin("In", this));
        outputs.add(new Pin("Out", this));

        inputs.get(0).setRelativePosition(0f, 0f);
        outputs.get(0).setRelativePosition(0f, 0f);
    }

    @Override
    public void update(Graph graph) {
        // Reicht das Signal ohne Verzögerung (Delay = 0) vom Eingang an den Ausgang weiter
        Pin inPin = inputs.get(0);
        Pin outPin = outputs.get(0);
        graph.queueEvent(outPin, inPin.getState(), 0);
    }
}