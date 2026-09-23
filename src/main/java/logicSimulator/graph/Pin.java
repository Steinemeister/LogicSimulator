package logicSimulator.graph;

import imgui.ImVec2;

import java.util.UUID;

public class Pin {
    private final UUID id;
    private final Node parentNode;
    private final PinType type;
    private PinState state;

    private ImVec2 position;

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

    public ImVec2 getPosition() {
        return  position;
    }

    public void setPosition(ImVec2 position) {
        this.position = position;
    }
}
