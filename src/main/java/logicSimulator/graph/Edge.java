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

        // Wenn es eine komplett gerade Linie ist
        if (x1 == x2 || y1 == y2) {
            return isPointNearSegment(px, py, x1, y1, x2, y2, tolerance);
        }

        // Bei einem S-Knick berechnen wir den X-Mittelpunkt (exakt wie beim Zeichnen)
        float midX = x1 + (x2 - x1) / 2f;

        // Wir prüfen nacheinander alle drei Segmente des Kabels:
        // Segment 1: Horizontal vom Start bis zur Mitte (x1, y1) -> (midX, y1)
        if (isPointNearSegment(px, py, x1, y1, midX, y1, tolerance)) return true;

        // Segment 2: Vertikal auf der Mittellinie (midX, y1) -> (midX, y2)
        if (isPointNearSegment(px, py, midX, y1, midX, y2, tolerance)) return true;

        // Segment 3: Horizontal von der Mitte bis zum Ziel (midX, y2) -> (x2, y2)
        return isPointNearSegment(px, py, midX, y2, x2, y2, tolerance);
    }

    private boolean isPointNearSegment(float px, float py, float x1, float y1, float x2, float y2, float tolerance) {
        float minX = Math.min(x1, x2) - tolerance;
        float maxX = Math.max(x1, x2) + tolerance;
        float minY = Math.min(y1, y2) - tolerance;
        float maxY = Math.max(y1, y2) + tolerance;

        // Liegt der Klick überhaupt im groben Rechteck der Teilstrecke?
        if (px < minX || px > maxX || py < minY || py > maxY) {
            return false;
        }

        // Wenn das Segment horizontal ist (y1 == y2)
        if (y1 == y2) {
            return Math.abs(py - y1) <= tolerance;
        }
        // Wenn das Segment vertikal ist (x1 == x2)
        if (x1 == x2) {
            return Math.abs(px - x1) <= tolerance;
        }

        return false;
    }

    public UUID getSourcePinId() { return sourcePinId; }
    public UUID getDestPinId() { return destPinId; }
}
