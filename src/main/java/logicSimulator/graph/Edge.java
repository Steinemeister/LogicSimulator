package logicSimulator.graph;

import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.Node;

import java.util.UUID;

public class Edge {
    private final UUID sourcePinId;
    private final UUID destPinId;

    public Edge(Pin sourcePin, Pin destPin) {
        this.sourcePinId = sourcePin.getId();
        this.destPinId = destPin.getId();
    }

    public void transmitSignal(Graph graph) {
        Pin src = graph.findPinGlobally(sourcePinId);
        Pin dest = graph.findPinGlobally(destPinId);

        if (src != null && dest != null) {
            graph.queueEvent(dest, src.getState(), 0);
        }
    }

    public UUID getSourcePinId() { return sourcePinId; }
    public UUID getDestPinId() { return destPinId; }
}
