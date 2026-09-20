package logicSimulator.graph;

import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.SimulationEvent;
import logicSimulator.graph.nodes.Node;

import java.util.*;

public class Graph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final PriorityQueue<SimulationEvent> eventQueue = new PriorityQueue<>();
    private long currentTick = 0;

    public void addNode(Node node) { nodes.add(node); }
    public void addEdge(Edge edge) { edges.add(edge); }
    public void removeEdge(Edge edge) { edges.remove(edge); }

    public Pin findPinGlobally(UUID pinId) {
        for (Node node : nodes) {
            Pin p = node.findPinById(pinId);
            if (p != null) return p;
        }
        return null;
    }

    public void queueEvent(Pin pin, Pin.State newState, long delay) {
        eventQueue.add(new SimulationEvent(currentTick + delay, pin, newState));
    }

    public boolean step() {
        if (eventQueue.isEmpty()) return false;

        // Wenn das nächste Event in der Zukunft liegt, spulen wir die Zeit vor
        if (eventQueue.peek().getTick() > currentTick) {
            currentTick = eventQueue.peek().getTick();
        }

        // Sammle ALLE Events für den aktuellen Tick
        List<SimulationEvent> currentEvents = new ArrayList<>();
        while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= currentTick) {
            currentEvents.add(eventQueue.poll());
        }

        List<Node> nodesToUpdate = new ArrayList<>();

        // Verarbeite die Events
        for (SimulationEvent event : currentEvents) {
            Pin pin = event.getPin();
            if (pin.getState() != event.getNewState()) {
                triggerPinChange(pin, event.getNewState(), nodesToUpdate);
            }
        }

        // Berechne die Logik-Knoten (AND, NOT, CustomModule)
        for (Node node : nodesToUpdate) {
            node.update(this);
        }

        return !eventQueue.isEmpty();
    }

    private void triggerPinChange(Pin pin, Pin.State newState, List<Node> nodesToUpdate) {
        pin.setState(newState);

        // Wenn es ein Gatter-Eingang ist, muss das Gatter rechnen
        if (pin.getOwner() != null && !(pin.getOwner() instanceof JunctionNode)) {
            if (!nodesToUpdate.contains(pin.getOwner())) {
                nodesToUpdate.add(pin.getOwner());
            }
        }

        // Signal über Kabel weiterleiten
        for (Edge edge : edges) {
            if (edge.getSourcePinId().equals(pin.getId())) {
                Pin destPin = findPinGlobally(edge.getDestPinId());
                if (destPin != null) {
                    if (destPin.getOwner() instanceof JunctionNode) {
                        // Junctions leiten das Signal verzögerungsfrei (rekursiv) weiter
                        if (destPin.getState() != newState) {
                            triggerPinChange(destPin, newState, nodesToUpdate);
                        }
                    } else {
                        // Normale Gatter erhalten ein Event für JETZT (delay = 0)
                        queueEvent(destPin, newState, 0);
                    }
                }
            }
        }
    }

    public void propagateSignals() {
        int maxSafetyLoops = 5000;
        // WICHTIG: Wir simulieren so lange, wie Events in der Queue existieren.
        // Erst wenn step() false zurückgibt UND die Queue wirklich leer ist, stoppen wir.
        while (!eventQueue.isEmpty() && maxSafetyLoops > 0) {
            step();
            // Erhöhe den Tick erst, wenn für den aktuellen Tick wirklich alle
            // Kettenreaktionen (0-Tick-Events) abgearbeitet wurden!
            if (!eventQueue.isEmpty() && eventQueue.peek().getTick() > currentTick) {
                currentTick++;
            }
            maxSafetyLoops--;
        }
    }

    public void initializeSimulation() {
        currentTick = 0;
        eventQueue.clear();
        for (Node node : nodes) {
            node.update(this);
        }
        propagateSignals();
    }

    public Node getNodeAt(float mx, float my) {
        // Rückwärts durchlaufen für die korrekte Tiefenreihenfolge (Vordergrund zuerst)
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);

            if (node instanceof JunctionNode) {
                // Junctions haben eine Größe von 0x0. Wir prüfen den Abstand zum Pin.
                Pin p = node.getInputs().get(0);
                float dx = p.getAbsoluteX() - mx;
                float dy = p.getAbsoluteY() - my;
                float clickRadius = 10f; // 10 Pixel Toleranz zum Greifen der Junction
                if ((dx * dx + dy * dy) <= (clickRadius * clickRadius)) {
                    return node;
                }
            } else {
                // Normale Gatter über die Box prüfen
                if (node.contains(mx, my)) {
                    return node;
                }
            }
        }
        return null;
    }

    public Edge getEdgeAt(float mx, float my, float tolerance) {
        for (Edge edge : edges) {
            if (edge.isPointNearLine(mx, my, tolerance, this)) {
                return edge;
            }
        }
        return null;
    }

    public Pin getAnyPinAt(float mx, float my, float radius) {
        for (Node node : nodes) {
            Pin pin = node.getPinAt(mx, my, radius);
            if (pin != null) {
                return pin;
            }
        }
        return null;
    }

    public JunctionNode splitEdgeWithJunction(Edge edgeToSplit, String junctionName, float mx, float my, float gridSize) {
        Pin originalSrc = findPinGlobally(edgeToSplit.getSourcePinId());
        Pin originalDest = findPinGlobally(edgeToSplit.getDestPinId());

        // Linien-Endpunkte holen
        float x1 = originalSrc.getAbsoluteX();
        float y1 = originalSrc.getAbsoluteY();
        float x2 = originalDest.getAbsoluteX();
        float y2 = originalDest.getAbsoluteY();

        // Mathematische Projektion der Maus (mx, my) auf die Strecke (x1,y1) -> (x2,y2)
        float l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        float t = 0f;
        if (l2 != 0) {
            t = ((mx - x1) * (x2 - x1) + (my - y1) * (y2 - y1)) / l2;
            t = Math.max(0f, Math.min(1f, t));
        }

        // Das ist der exakte Punkt AUF dem Kabel
        float lineX = x1 + t * (x2 - x1);
        float lineY = y1 + t * (y2 - y1);

        // Raster-Einrastung für diesen Linienpunkt berechnen
        float snappedX = Math.round(lineX / gridSize) * gridSize;
        float snappedY = Math.round(lineY / gridSize) * gridSize;

        // Altes Kabel entfernen
        removeEdge(edgeToSplit);

        // Neue Junction erstellen und exakt auf der Linie platzieren
        JunctionNode newJunction = new JunctionNode(junctionName);
        newJunction.setPosition(snappedX, snappedY);
        addNode(newJunction);

        Pin junctionPin = newJunction.getInputs().get(0);

        // Neue Kabelsegmente legen
        addEdge(new Edge(originalSrc, junctionPin));
        addEdge(new Edge(junctionPin, originalDest));

        // Live-Zustand injizieren
        List<Node> nodesToUpdate = new ArrayList<>();
        triggerPinChange(originalSrc, originalSrc.getState(), nodesToUpdate);
        for (Node n : nodesToUpdate) {
            n.update(this);
        }
        propagateSignals();

        return newJunction;
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}