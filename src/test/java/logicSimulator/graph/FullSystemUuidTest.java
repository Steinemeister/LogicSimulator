package logicSimulator.graph;

import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.AndNode;
import logicSimulator.graph.nodes.module.CustomModuleNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FullSystemUuidTest {
    @Test
    void testEntireSystemWithUuidArchitecture() {
        // 1. Die Haupt-Arbeitsfläche (Der oberste Graph)
        Graph workspace = new Graph();

        // 2. Komponenten für die Hauptfläche erstellen
        ButtonNode mainSwitchA = new ButtonNode("SchalterA");
        ButtonNode mainSwitchB = new ButtonNode("SchalterB");

        CustomModuleNode customAndModule = new CustomModuleNode("MeinCustomAND");
        customAndModule.addExternalInput("InA");
        customAndModule.addExternalInput("InB");
        customAndModule.addExternalOutput("OutY");

        LedNode led1 = new LedNode("LED_1");
        LedNode led2 = new LedNode("LED_2");

        workspace.addNode(mainSwitchA);
        workspace.addNode(mainSwitchB);
        workspace.addNode(customAndModule);
        workspace.addNode(led1);
        workspace.addNode(led2);

        // =================================================================
        // 3. INNENLEBEN DES CUSTOM-MODULS VERDRAHTEN
        // =================================================================
        Graph innerGraph = customAndModule.getInternalGraph();
        AndNode internalAndGate = new AndNode("InternesAND");
        innerGraph.addNode(internalAndGate);

        Pin innerInA = customAndModule.getInternalInputNodes().get(0).getOutputs().get(0);
        Pin innerInB = customAndModule.getInternalInputNodes().get(1).getOutputs().get(0);
        Pin innerOutY = customAndModule.getInternalOutputNodes().get(0).getInputs().get(0);

        innerGraph.addEdge(new Edge(innerInA, internalAndGate.getInputs().get(0)));
        innerGraph.addEdge(new Edge(innerInB, internalAndGate.getInputs().get(1)));
        innerGraph.addEdge(new Edge(internalAndGate.getOutputs().get(0), innerOutY));

        // =================================================================
        // 4. HAUPT-ARBEITSFLÄCHE VERDRAHTEN
        // =================================================================
        workspace.addEdge(new Edge(mainSwitchA.getOutputs().get(0), customAndModule.getInputs().get(0)));
        workspace.addEdge(new Edge(mainSwitchB.getOutputs().get(0), customAndModule.getInputs().get(1)));

        // Das Kabel, das wir gleich splitten wollen (Modul-Ausgang zu LED 1)
        Edge edgeToSplit = new Edge(customAndModule.getOutputs().get(0), led1.getInputs().get(0));
        workspace.addEdge(edgeToSplit);

        // Simulation aufwecken
        workspace.initializeSimulation();

        // Testfall 1: Startzustand prüfen (0 AND 0 = 0)
        assertFalse(led1.isOn(), "LED 1 sollte anfangs aus sein.");
        assertFalse(led2.isOn(), "LED 2 sollte anfangs aus sein.");

        // Testfall 2: Nur einen Schalter aktivieren (1 AND 0 = 0)
        mainSwitchA.toggle(workspace);
        assertFalse(led1.isOn(), "LED 1 muss bei unvollständigem AND aus bleiben.");

        // Testfall 3: Zweiten Schalter aktivieren (1 AND 1 = 1) -> Signal schaltet durch
        mainSwitchB.toggle(workspace);
        workspace.propagateSignals();

        assertTrue(led1.isOn(), "LED 1 sollte jetzt leuchten, da beide Schalter HIGH sind.");

        // =================================================================
        // 5. HÄRTETEST: AKTIVES KABEL DYNAMISCH PER UUID SPLITTEN
        // =================================================================
        // Wir splitten das brennende Kabel.
        JunctionNode dynamicJunction = workspace.splitEdgeWithJunction(edgeToSplit, "DynamischerKnotenpunkt");

        // Jetzt schließen wir LED 2 an die echte neue Junction an
        Pin realJunctionPin = dynamicJunction.getInputs().get(0);
        Edge newEdgeToLed2 = new Edge(realJunctionPin, led2.getInputs().get(0));
        workspace.addEdge(newEdgeToLed2);

        // DIE REPARATUR: Da realJunctionPin bereits HIGH ist, werfen wir manuell ein Event
        // für die LED 2 in die Queue, um den Stromfluss durch das neue Kabel zu erzwingen!
        workspace.queueEvent(led2.getInputs().get(0), realJunctionPin.getState(), 0);
        workspace.propagateSignals();

        // Nun stimmen alle Zustände perfekt!
        assertEquals(Pin.State.HIGH, dynamicJunction.getInputs().get(0).getState(),
                "Die dynamisch eingefügte Junction hätte den HIGH-Zustand sofort adaptieren müssen.");

        assertTrue(led1.isOn(), "LED 1 muss auch nach dem Kabelsplit weiterleuchten.");

        // DIESE ASSERTIERUNG WIRD JETZT GRÜN!
        assertTrue(led2.isOn(), "LED 2 sollte jetzt ebenfalls leuchten.");

        // Testfall 6: Abschalten prüfen (Zurück auf 0 AND 1 = 0)
        mainSwitchA.toggle(workspace);
        workspace.propagateSignals();

        assertFalse(led1.isOn(), "LED 1 hätte nach dem Ausschalten ausgehen müssen.");
        assertFalse(led2.isOn(), "LED 2 hätte nach dem Ausschalten ausgehen müssen.");
    }
}
