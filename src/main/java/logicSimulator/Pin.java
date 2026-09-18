package logicSimulator;

public class Pin {
    public enum State { LOW, HIGH }

    private final String name;
    private State state = State.LOW;

    public Pin(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
}
