package logicSimulator;

import logicSimulator.graph.nodes.Pin;

public class SimulationEvent implements Comparable<SimulationEvent> {
    private final long tick;
    private final Pin pin;
    private final Pin.State newState;

    public SimulationEvent(long tick, Pin pin, Pin.State newState) {
        this.tick = tick;
        this.pin = pin;
        this.newState = newState;
    }

    public long getTick() { return tick; }
    public Pin getPin() { return pin; }
    public Pin.State getNewState() { return newState; }

    @Override
    public int compareTo(SimulationEvent o) {
        return Long.compare(this.tick, o.tick);
    }
}