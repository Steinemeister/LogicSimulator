package logicSimulator.graph.nodes;

import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JunctionNodeTest {
    @Test
    void testSignalSplittingThroughJunction() {
        Graph graph = new Graph();

        ButtonNode button = new ButtonNode("Button");
        JunctionNode junction = new JunctionNode("Junction");
        LedNode ledA = new LedNode("LEDA");
        LedNode ledB = new LedNode("LEDB");

        graph.addNode(button);
        graph.addNode(junction);
        graph.addNode(ledA);
        graph.addNode(ledB);

        // Verdrahtung: Ein Eingang in die Junction, zwei Ausgänge zu den LEDs
        graph.addEdge(new Edge(button, "Out", junction, "Point"));
        graph.addEdge(new Edge(junction, "Point", ledA, "In"));
        graph.addEdge(new Edge(junction, "Point", ledB, "In"));

        graph.initializeSimulation();

        // Beide LEDs müssen anfangs aus sein
        assertFalse(ledA.isOn());
        assertFalse(ledB.isOn());

        // Button aktivieren
        button.toggle(graph);

        // Beide LEDs müssen das Signal zeitgleich erhalten haben
        assertTrue(ledA.isOn(), "LED A hat das Signal von der Junction nicht empfangen.");
        assertTrue(ledB.isOn(), "LED B hat das Signal von der Junction nicht empfangen.");
    }
}
