package logicSimulator.graph.nodes;

import logicSimulator.graph.Graph;
import logicSimulator.graph.Node;
import logicSimulator.graph.Pin;

public class NotNode extends Node {
    public NotNode(String nodeTypeName) {
        super(nodeTypeName);
        this.getInputPins().add(new Pin(this, Pin.PinType.INPUT));
        this.getOutputPins().add(new Pin(this, Pin.PinType.OUTPUT));
    }

    @Override
    public void update(Graph graph) {
        if (this.getInputPins().getFirst().getState() == this.getOutputPins().getFirst().getState()) {
            Pin output = this.getOutputPins().getFirst();
            Pin.PinState newState = output.getState() == Pin.PinState.HIGH ? Pin.PinState.LOW : Pin.PinState.HIGH;
            graph.queueEvent(output, 1,newState);
        }
    }
}
