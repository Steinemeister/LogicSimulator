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

    public void queueEvent(Pin pin, Pin.State newState, long delay) {
        eventQueue.add(new SimulationEvent(currentTick + delay, pin, newState));
    }

    public boolean step() {
        if (eventQueue.isEmpty()) return false;

        // Falls Events in der Zukunft liegen, spulen wir die Zeit vor
        if (eventQueue.peek().getTick() > currentTick) {
            currentTick = eventQueue.peek().getTick();
        }

        List<SimulationEvent> currentEvents = new ArrayList<>();
        while (!eventQueue.isEmpty() && eventQueue.peek().getTick() <= currentTick) {
            currentEvents.add(eventQueue.poll());
        }

        List<Node> nodesToUpdate = new ArrayList<>();

        for (SimulationEvent event : currentEvents) {
            Pin pin = event.getPin();
            if (pin.getState() != event.getNewState()) {
                pin.setState(event.getNewState());

                if (pin.getOwner() != null && !nodesToUpdate.contains(pin.getOwner())) {
                    nodesToUpdate.add(pin.getOwner());
                }

                for (Edge edge : edges) {
                    if (edge.getSourceNode() == pin.getOwner() && edge.getSourcePinName().equals(pin.getName())) {
                        Pin destPin = edge.getDestNode().getInputs().get(edge.getDestPinName());
                        if (destPin != null) {
                            queueEvent(destPin, pin.getState(), 0);
                        }
                    }
                }
            }
        }

        // 2. Aktivierte Knoten ihre Logik berechnen lassen
        for (Node node : nodesToUpdate) {
            node.update(this);
        }

        return !eventQueue.isEmpty();
    }

    /**
     * Simuliert den Graphen so lange, bis keine Events mehr aktiv sind (Ruhezustand).
     */
    public void propagateSignals() {
        int maxSafetyLoops = 1000;
        // Solange Events da sind ODER noch Events für den exakt aktuellen Tick generiert wurden
        while (!eventQueue.isEmpty() && maxSafetyLoops > 0) {
            step();
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

    public JunctionNode splitEdgeWithJunction(Edge edgeToSplit, String junctionName) {
        // 1. Ursprüngliche Verbindungspartner merken
        Node originalSource = edgeToSplit.getSourceNode();
        String originalSourcePin = edgeToSplit.getSourcePinName();
        Node originalDest = edgeToSplit.getDestNode();
        String originalDestPin = edgeToSplit.getDestPinName();

        // 2. Altes Kabel entfernen
        removeEdge(edgeToSplit);

        // 3. Neue Junction erstellen und hinzufügen
        JunctionNode newJunction = new JunctionNode(junctionName);
        addNode(newJunction);

        // 4. Die beiden neuen Kabelstücke erstellen und hinzufügen
        Edge sourceToJunction = new Edge(originalSource, originalSourcePin, newJunction, "Point");
        Edge junctionToDest = new Edge(newJunction, "Point", originalDest, originalDestPin);

        addEdge(sourceToJunction);
        addEdge(junctionToDest);

        // 5. Signal-Zustand manuell und sofort durchdrücken:
        // Wir holen den aktuellen Live-Zustand des Quell-Pins...
        Pin.State currentSourceState = originalSource.getOutputs().get(originalSourcePin).getState();

        // ...und zwingen das erste neue Kabel, diesen Zustand sofort als Event an die Junction zu senden.
        Pin junctionPin = newJunction.getInputs().get("Point");
        queueEvent(junctionPin, currentSourceState, 0);

        // Simulation anwerfen, damit die Junction das Event verarbeitet und an Kabel 2 weiterreicht
        propagateSignals();

        return newJunction;
    }

    private void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
    public long getCurrentTick() { return currentTick; }
}