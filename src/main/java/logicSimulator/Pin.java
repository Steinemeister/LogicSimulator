package logicSimulator;

import logicSimulator.nodes.Node;

public class Pin {
    public enum State { LOW, HIGH }

    private final String name;
    private final Node owner;
    private State state = State.LOW;

    public Pin(String name, Node owner) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() { return name; }
    public Node getOwner() { return owner; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
}