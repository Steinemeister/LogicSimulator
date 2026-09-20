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

    public JunctionNode splitEdgeWithJunction(Edge edgeToSplit, String junctionName) {
        Pin originalSrc = findPinGlobally(edgeToSplit.getSourcePinId());
        Pin originalDest = findPinGlobally(edgeToSplit.getDestPinId());

        // 1. Altes Kabel entfernen
        removeEdge(edgeToSplit);

        // 2. Neue Junction erstellen und hinzufügen
        JunctionNode newJunction = new JunctionNode(junctionName);
        addNode(newJunction);

        Pin junctionPin = newJunction.getInputs().get(0);

        // 3. Die zwei neuen Kabelsegmente registrieren
        addEdge(new Edge(originalSrc, junctionPin));
        addEdge(new Edge(junctionPin, originalDest));

        // 4. UNFEHLBARER SIGNAL-INJEKTOR:
        // Wir holen den aktuellen Live-Zustand des Modul-Ausgangs (der ist HIGH)
        Pin.State currentSourceState = originalSrc.getState();

        // Wir zwingen das Event-System, diesen Zustand als neues Event direkt für den Junction-Pin einzutragen.
        // Da der Junction-Pin frisch erstellt wurde, steht er auf LOW. Der Wechsel LOW -> HIGH wird GARANTIERT getriggert!
        queueEvent(junctionPin, currentSourceState, 0);

        // 5. Die Simulation pulsieren lassen, damit die Junction das Signal verarbeitet
        propagateSignals();

        return newJunction;
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}