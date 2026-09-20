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

    public boolean isPointNearLine(float px, float py, float tolerance, Graph graph) {
        Pin src = graph.findPinGlobally(this.sourcePinId);
        Pin dest = graph.findPinGlobally(this.destPinId);

        if (src == null || dest == null) return false;

        float x1 = src.getAbsoluteX();
        float y1 = src.getAbsoluteY();
        float x2 = dest.getAbsoluteX();
        float y2 = dest.getAbsoluteY();

        float minX = Math.min(x1, x2) - tolerance;
        float maxX = Math.max(x1, x2) + tolerance;
        float minY = Math.min(y1, y2) - tolerance;
        float maxY = Math.max(y1, y2) + tolerance;

        // Liegt die Maus überhaupt im groben Bereich der Linie?
        if (px < minX || px > maxX || py < minY || py > maxY) {
            return false;
        }

        // Wenn das Segment horizontal verläuft (y1 == y2)
        if (y1 == y2) {
            return Math.abs(py - y1) <= tolerance;
        }
        // Wenn das Segment vertikal verläuft (x1 == x2)
        if (x1 == x2) {
            return Math.abs(px - x1) <= tolerance;
        }

        return false;
    }

    public UUID getSourcePinId() { return sourcePinId; }
    public UUID getDestPinId() { return destPinId; }
}
