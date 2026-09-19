package logicSimulator.graph;

import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.logic.AndNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeLayoutTest {
    @Test
    void testStandardGateLayout() {
        // Das AND-Gatter nutzt im Konstruktor calculateLayout(24f)
        // inputs: 2, outputs: 1. Max = 2 Pins.
        // Erwartete Höhe: 2 * 24f = 48f.
        AndNode andNode = new AndNode("TestAND");

        // 1. Dimensionen prüfen
        assertEquals(80f, andNode.getWidth(), "Breite sollte dem Standard entsprechen.");
        assertEquals(48f, andNode.getHeight(), "Höhe muss genau 48f (2 * 24f) betragen.");

        // 2. Inputs am linken Rand prüfen (X = 0)
        Pin pinA = andNode.getInputs().get("A");
        Pin pinB = andNode.getInputs().get("B");

        assertEquals(0f, pinA.getRelX(), "Input A muss am linken Rand liegen.");
        assertEquals(12f, pinA.getRelY(), "Input A sollte zentriert im ersten Slot (Y=12) liegen.");

        assertEquals(0f, pinB.getRelX(), "Input B muss am linken Rand liegen.");
        assertEquals(36f, pinB.getRelY(), "Input B sollte zentriert im zweiten Slot (Y=36) liegen.");

        // 3. Output am rechten Rand prüfen (X = width)
        Pin pinOut = andNode.getOutputs().get("Out");
        assertEquals(80f, pinOut.getRelX(), "Output muss am rechten Rand (X=80) liegen.");
        assertEquals(12f, pinOut.getRelY(), "Da nur ein Output existiert, liegt er im ersten Slot (Y=12).");
    }

    @Test
    void testAsymmetricModuleLayout() {
        // Wir simulieren ein Custom-Modul mit 4 Inputs und 2 Outputs
        // Dazu erstellen wir eine anonyme Unterklasse von Node, um calculateLayout direkt zu testen
        Node customNode = new Node("Custom") {
            @Override
            public void update(Graph graph) {}
        };

        // Pins hinzufügen
        customNode.getInputs().put("In1", new Pin("In1", customNode));
        customNode.getInputs().put("In2", new Pin("In2", customNode));
        customNode.getInputs().put("In3", new Pin("In3", customNode));
        customNode.getInputs().put("In4", new Pin("In4", customNode));

        customNode.getOutputs().put("Out1", new Pin("Out1", customNode));
        customNode.getOutputs().put("Out2", new Pin("Out2", customNode));

        // Layout berechnen mit 20px Abstand pro Pin. Max Pins = 4.
        // Erwartete Höhe: 4 * 20f = 80f.
        customNode.calculateLayout(20f);

        assertEquals(80f, customNode.getHeight(), "Höhe muss durch die 4 Inputs bestimmt werden (4 * 20 = 80).");

        // Letzten Input prüfen (4. Slot bei 20px Abstand -> 3 * 20 + 10 = 70)
        Pin in4 = customNode.getInputs().get("In4");
        assertEquals(70f, in4.getRelY(), "Der vierte Input muss im vierten Slot zentriert sein.");

        // Outputs prüfen (Es gibt nur 2, sie teilen sich die Slots 1 und 2 der Höhe nach auf)
        Pin out1 = customNode.getOutputs().get("Out1");
        Pin out2 = customNode.getOutputs().get("Out2");

        assertEquals(10f, out1.getRelY(), "Erster Output im ersten Slot.");
        assertEquals(30f, out2.getRelY(), "Zweiter Output im zweiten Slot.");
    }

    @Test
    void testJunctionNodeLayoutExemption() {
        // Die JunctionNode überschreibt das Kasten-Layout komplett
        JunctionNode junction = new JunctionNode("Knotenpunkt");

        assertEquals(0f, junction.getWidth(), "Junction-Breite muss 0 sein.");
        assertEquals(0f, junction.getHeight(), "Junction-Höhe muss 0 sein.");

        Pin pointPin = junction.getInputs().get("Point");
        assertEquals(0f, pointPin.getRelX(), "Junction-Pin muss im Zentrum liegen.");
        assertEquals(0f, pointPin.getRelY(), "Junction-Pin muss im Zentrum liegen.");
    }

    @Test
    void testAbsolutePositionUpdates() {
        AndNode andNode = new AndNode("MovingAND");

        // Verschiebe den Knoten im Raum
        andNode.setPosition(150f, 200f);

        Pin pinA = andNode.getInputs().get("A");
        Pin pinOut = andNode.getOutputs().get("Out");

        // Absolute Koordinaten abfragen: Globaler Offset + Relativer Offset
        // pinA rel: (0, 12) -> abs: (150+0, 200+12)
        assertEquals(150f, pinA.getAbsoluteX(), "Absolute X-Koordinate des Inputs stimmt nicht.");
        assertEquals(212f, pinA.getAbsoluteY(), "Absolute Y-Koordinate des Inputs stimmt nicht.");

        // pinOut rel: (80, 24) -> abs: (150+80, 200+24)
        assertEquals(230f, pinOut.getAbsoluteX(), "Absolute X-Koordinate des Outputs stimmt nicht.");
        assertEquals(212f, pinOut.getAbsoluteY(), "Absolute Y-Koordinate des Outputs stimmt nicht.");
    }
}
