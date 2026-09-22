package logicSimulator.graph.nodes;

import logicSimulator.graph.Node;
import logicSimulator.graph.Pin;

import java.util.List;

public class NotNode extends Node {
    public NotNode(String nodeTypeName) {
        super(nodeTypeName);
        this.getInputPins().add(new Pin(this, Pin.PinType.INPUT));
        this.getOutputPins().add(new Pin(this, Pin.PinType.OUTPUT));
    }

    @Override
    public List<Pin> update() {
        Pin.PinState lastState = this.getOutputPins().getFirst().getState();

        Pin.PinState newState = this.getInputPins().getFirst().getState() == Pin.PinState.LOW ? Pin.PinState.HIGH : Pin.PinState.LOW;

        if (lastState == newState) {
            return List.of();
        }

        this.getOutputPins().getFirst().setState(newState);

        return List.of(this.getOutputPins().getFirst());
    }
}
