package logicSimulator.graph.nodes.logic;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class OrNode extends Node {
    public OrNode(String name) {
        super(name);

        // Pins in die Listen eintragen
        inputs.add(new Pin("A", this));
        inputs.add(new Pin("B", this));
        outputs.add(new Pin("Out", this));

        // Nutze den automatischen Layout-Algorithmus mit 20px Grid
        calculateLayout(20f);
    }

    @Override
    public void update(Graph graph) {
        boolean a = inputs.get(0).getState() == Pin.State.HIGH;
        boolean b = inputs.get(1).getState() == Pin.State.HIGH;

        // Logische ODER-Verknüpfung
        Pin.State result = (a || b) ? Pin.State.HIGH : Pin.State.LOW;
        graph.queueEvent(outputs.get(0), result, 1);
    }
}