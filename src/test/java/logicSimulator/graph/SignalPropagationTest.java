package logicSimulator.graph;

import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.NotNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignalPropagationTest {
    @Test
    void testButtonToInverterToLedChain() {
        Graph graph = new Graph();

        ButtonNode button = new ButtonNode("Button");
        NotNode inverter = new NotNode("Inverter");
        LedNode led = new LedNode("LED");

        graph.addNode(button);
        graph.addNode(inverter);
        graph.addNode(led);

        graph.addEdge(new Edge(button, "Out", inverter, "In"));
        graph.addEdge(new Edge(inverter, "Out", led, "In"));

        // 1. Initialisierung prüfen (Button ist LOW -> Inverter macht HIGH -> LED leuchtet)
        graph.initializeSimulation();
        assertTrue(led.isOn(), "LED sollte initial an sein, da der Inverter das LOW umkehrt.");

        // 2. Zustand umschalten (Button auf HIGH -> Inverter macht LOW -> LED geht aus)
        button.toggle(graph);
        assertFalse(led.isOn(), "LED sollte nach dem Einschalten des Buttons ausgehen.");

        // 3. Zurückschalten
        button.toggle(graph);
        assertTrue(led.isOn(), "LED sollte wieder angehen, wenn der Button losgelassen wird.");
    }
}
