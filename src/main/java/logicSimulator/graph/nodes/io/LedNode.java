package logicSimulator.graph.nodes.io;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class LedNode extends Node {
    public LedNode(String name) {
        super(name);
        // Eingangs-Pin hinzufügen
        inputs.add(new Pin("In", this));

        // Layout berechnen
        calculateLayout(24f);

        // Optische Verschönerung: LEDs quadratisch machen
        this.height = 50f;
        this.width = 50f;

        // Pin in der Mitte der linken Kante platzieren
        inputs.get(0).setRelativePosition(0f, this.height / 2f);
    }

    public boolean isOn() {
        return inputs.get(0).getState() == Pin.State.HIGH;
    }

    @Override
    public void update(Graph graph) {
        // Die LED reagiert passiv, sie muss selbst keine neuen Events erzeugen
    }
}
