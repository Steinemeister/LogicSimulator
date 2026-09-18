package logicSimulator.nodes;

import logicSimulator.Pin;

public class SplitterNode extends Node {
    public SplitterNode(String name, int outputCount) {
        super(name);
        inputs.put("In", new Pin("In"));
        for (int i = 0; i < outputCount; i++) {
            outputs.put("Out_" + i, new Pin("Out_" + i));
        }
    }

    @Override
    public void update() {
        Pin.State inputState = inputs.get("In").getState();
        for (Pin out : outputs.values()) {
            out.setState(inputState);
        }
    }
}
