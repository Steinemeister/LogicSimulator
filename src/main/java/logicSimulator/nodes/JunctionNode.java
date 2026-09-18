package logicSimulator.nodes;

import logicSimulator.Pin;

public class JunctionNode extends Node {
    public JunctionNode(String name, int inputCount) {
        super(name);
        // Dieser Knoten nimmt mehrere Leitungen auf
        for (int i = 0; i < inputCount; i++) {
            inputs.put("In_" + i, new Pin("In_" + i));
        }
        // ... und führt sie zu einem Ausgang zusammen
        outputs.put("Out", new Pin("Out"));
    }

    @Override
    public void update() {
        Pin out = outputs.get("Out");

        // Wenn mindestens ein Eingang HIGH ist, wird der Ausgang HIGH (Wired-OR)
        boolean anyHigh = false;
        for (Pin in : inputs.values()) {
            if (in.getState() == Pin.State.HIGH) {
                anyHigh = true;
                break;
            }
        }

        out.setState(anyHigh ? Pin.State.HIGH : Pin.State.LOW);
    }
}