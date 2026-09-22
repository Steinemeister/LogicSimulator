package logicSimulator.graph;

import java.util.UUID;

public class Pin {
    private final UUID id;
    private final Node parentNode;
    private final PinType type;
    private PinState state;

    public enum PinType { INPUT, OUTPUT }

    public enum PinState { LOW, HIGH }

    public Pin(Node parentNode, PinType type) {
        this.id = UUID.randomUUID();
        this.parentNode = parentNode;
        this.type = type;
        this.state = PinState.LOW;
    }

    public UUID getId() {
        return id;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public PinType getType() {
        return type;
    }

    public PinState getState() {
        return state;
    }

    public void setState(PinState state) {
        this.state = state;
    }
}
