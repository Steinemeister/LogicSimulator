package logicSimulator.graph.nodes.io;

import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class ButtonNode extends Node {
    private boolean isPressed = false;

    public ButtonNode(String name) {
        super(name);
        outputs.put("Out", new Pin("Out", this));
    }

    public void toggle(Graph graph) {
        isPressed = !isPressed;
        Pin.State newState = isPressed ? Pin.State.HIGH : Pin.State.LOW;

        // Wir werfen die Änderung des Buttons in die Queue
        graph.queueEvent(outputs.get("Out"), newState, 0);
        graph.propagateSignals();
    }

    public boolean isPressed() { return isPressed; }

    @Override
    public void update(Graph graph) {
        // Schreibt beim allerersten Start den aktuellen Zustand in die Queue
        graph.queueEvent(outputs.get("Out"), isPressed ? Pin.State.HIGH : Pin.State.LOW, 0);
    }
}
