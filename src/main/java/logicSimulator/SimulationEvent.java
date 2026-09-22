package logicSimulator;

import logicSimulator.graph.Pin;

public record SimulationEvent(long tick, Pin pin) implements Comparable<SimulationEvent> {
    @Override
    public int compareTo(SimulationEvent other) {
        return Long.compare(this.tick, other.tick);
    }
}
