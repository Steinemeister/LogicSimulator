package logicSimulator.graph.nodes;

public class Pin {
    public enum State { LOW, HIGH }

    private final String name;
    private final Node owner;
    private State state = State.LOW;

    private float relX;
    private float relY;

    public Pin(String name, Node owner) {
        this.name = name;
        this.owner = owner;
    }

    public void setRelativePosition(float relX, float relY) {
        this.relX = relX;
        this.relY = relY;
    }

    public float getAbsoluteX() {
        return owner != null ? owner.getX() + relX : relX;
    }

    public float getAbsoluteY() {
        return owner != null ? owner.getY() + relY : relY;
    }

    public String getName() { return name; }
    public Node getOwner() { return owner; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public float getRelX() { return relX; }
    public float getRelY() { return relY; }
}