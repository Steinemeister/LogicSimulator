package logicSimulator.nodes;

import logicSimulator.Pin;

public class NotNode extends Node {
    public NotNode(String name) {
        super(name);
        inputs.put("In", new Pin("In"));
        outputs.put("Out", new Pin("Out"));
    }

    @Override
    public void update() {
        Pin in = inputs.get("In");
        Pin out = outputs.get("Out");
        out.setState(in.getState() == Pin.State.LOW ? Pin.State.HIGH : Pin.State.LOW);
    }
}
