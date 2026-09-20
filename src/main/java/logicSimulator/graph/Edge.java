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

        float l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (l2 == 0) return false;

        float t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));

        float projX = x1 + t * (x2 - x1);
        float projY = y1 + t * (y2 - y1);
        float distance = (float) Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));

        return distance <= tolerance;
    }

    public UUID getSourcePinId() { return sourcePinId; }
    public UUID getDestPinId() { return destPinId; }
}
