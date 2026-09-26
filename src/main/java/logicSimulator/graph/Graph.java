package logicSimulator.graph;

import logicSimulator.SimulationEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Graph {
    private final Map<UUID, Node> nodes;
    private final Map<UUID, Edge> edges;

    private final PriorityQueue<SimulationEvent> eventQueue = new PriorityQueue<>();
    private long currentTick = 0;

    public Graph() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
    }

    public void queueEvent(Pin pin, long delay, Pin.PinState newState) {
        if (pin.getType() == Pin.PinType.INPUT) return;
        eventQueue.add(new SimulationEvent(currentTick + delay, pin, newState));
    }

    public void runSimulation() {
        System.out.println("starting simulation");
        while (step());
        System.out.println("done");
    }

    public boolean step() {
        List<Node> nodesToProcess = new ArrayList<>();
        while (!eventQueue.isEmpty() && eventQueue.peek().tick() == currentTick) {
            SimulationEvent event = eventQueue.poll();
            Pin pin = event.pin();
            pin.setState(event.newState());
            getEdgesConnectedToPin(pin).forEach(
                    edge -> edge.getTargets().forEach(
                            target -> {
                                target.setState(edge.getState());
                                nodesToProcess.add(target.getParentNode());
                            }
                    )
            );

        }

        Set<Node> nodesToUpdateSet = new HashSet<>(nodesToProcess);
        nodesToUpdateSet.forEach(node -> node.update(this));

        currentTick++;
        return !eventQueue.isEmpty();
    }

    public List<Edge> getEdgesConnectedToPin(Pin pin) {
        List<Edge> connectedEdges = new ArrayList<>();
        edges.values().forEach(edge -> {
            if (edge.getSource() == pin) connectedEdges.add(edge);
        });
        return connectedEdges;
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public void addEdge(Edge edge) {
        edges.put(edge.getId(), edge);
    }

    public Node getNode(UUID id) {
        return nodes.get(id);
    }

    public Edge getEdge(UUID id) {
        return edges.get(id);
    }

    public Map<UUID, Node> getNodes() {
        return nodes;
    }

    public List<Node> getNodesInside(float minX, float minY, float maxX, float maxY) {
        List<Node> result = new ArrayList<>();

        for (Node node : nodes.values()) {
            // X-Achse bleibt unverändert auf dem Integer-Grid
            float nodeMinX = node.getX();
            float nodeMaxX = node.getX() + node.getWidth();

            // KORREKTUR Y-Achse: Verschiebung um +0.5f für die visuelle Box einrechnen
            float nodeMinY = node.getY() + 0.5f;
            float nodeMaxY = node.getY() + node.getHeight() + 0.5f;

            // Überprüfe, ob sich die Auswahlbox mit der verschobenen Node-Box überschneidet
            if (nodeMinX < maxX && nodeMaxX > minX &&
                    nodeMinY < maxY && nodeMaxY > minY) {
                result.add(node);
            }
        }

        return result;
    }

    public Map<UUID, Edge> getEdges() {
        return edges;
    }
}
