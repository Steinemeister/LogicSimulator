package logicSimulator.graph.nodes.io;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class ButtonNode extends Node {
    private boolean isPressed = false;

    public ButtonNode(String name) {
        super(name);
        // Ausgangs-Pin hinzufügen
        outputs.add(new Pin("Out", this));

        // Automatische Pin-Verteilung berechnen (24px Abstand)
        calculateLayout(24f);

        // Optische Verschönerung: Buttons sollen etwas höher sein als ein einzelner Pin
        this.height = 50f;
        this.width = 60f;  // Schmaler, sieht mehr nach Button aus

        // Pins nach der manuellen Größenänderung kurz zentrieren
        outputs.get(0).setRelativePosition(this.width, this.height / 2f);
    }

    public void toggle(Graph graph) {
        isPressed = !isPressed;
        Pin.State newState = isPressed ? Pin.State.HIGH : Pin.State.LOW;

        // Wir werfen die Änderung des Buttons in die Queue
        graph.queueEvent(outputs.get(0), newState, 0);
        graph.propagateSignals();
    }

    public boolean isPressed() { return isPressed; }

    @Override
    public void update(Graph graph) {
        // Schreibt beim allerersten Start den aktuellen Zustand in die Queue
        graph.queueEvent(outputs.get(0), isPressed ? Pin.State.HIGH : Pin.State.LOW, 0);
    }
}
