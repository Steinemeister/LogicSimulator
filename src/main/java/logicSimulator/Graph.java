package logicSimulator;

import logicSimulator.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        // Sicherheits-Feature: Lösche alle Kabel, die an diesem Knoten hingen
        edges.removeIf(edge -> edge.getSourceNode() == node || edge.getDestNode() == node);
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    /**
     * Führt einen einzelnen Simulationsschritt für diesen Graphen aus.
     */
    public void step() {
        // 1. Signale über alle Kabel (Kanten) transportieren
        for (Edge edge : edges) {
            edge.transmitSignal();
        }

        // 2. Alle Knoten ihre innere Logik berechnen lassen
        for (Node node : nodes) {
            node.update();
        }
    }

    // Getter für das Rendering (LWJGL muss wissen, was gezeichnet werden soll)
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
