package logicSimulator.graph;

import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeSplittingTest {
    @Test
    void testSplitActiveEdgeWithoutSignalLoss() {
        Graph graph = new Graph();

        // 1. Setup: Ein Button direkt an einer LED
        ButtonNode button = new ButtonNode("PowerButton");
        LedNode led = new LedNode("TargetLED");

        graph.addNode(button);
        graph.addNode(led);

        // Verbinde sie mit einem direkten Kabel
        Edge directEdge = new Edge(button, "Out", led, "In");
        graph.addEdge(directEdge);

        // Simulation starten
        graph.initializeSimulation();

        // 2. Button einschalten -> Kabel wird HIGH -> LED leuchtet
        button.toggle(graph);
        assertTrue(led.isOn(), "Die LED sollte leuchten, bevor das Kabel gesplittet wird.");

        // 3. JETZT SPLITTEN wir das aktive Kabel mitten im Betrieb
        JunctionNode insertedJunction = graph.splitEdgeWithJunction(directEdge, "DynamicJunction");

        // 4. Überprüfen, ob das System stabil geblieben ist
        // Das Signal darf nicht abgebrochen sein: LED muss weiterhin HIGH sein!
        assertTrue(led.isOn(), "Die LED MUSS auch nach dem Splitten weiterleuchten!");

        // Auch der Universal-Pin der neuen Junction muss das HIGH übernommen haben
        assertEquals(Pin.State.HIGH, insertedJunction.getInputs().get("Point").getState(),
                "Die neue Junction hätte das HIGH-Signal sofort aufnehmen müssen.");

        // 5. Gegenprobe: Button wieder ausschalten -> Signal muss über die Junction hinweg abfallen
        button.toggle(graph);
        assertFalse(led.isOn(), "Die LED sollte ausgehen, wenn der Button nach dem Splitten abgeschaltet wird.");
    }
}
