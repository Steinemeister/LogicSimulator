package logicSimulator;

import imgui.app.Application;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.*;
import logicSimulator.rendering.EditorWindow;

public class Main {
    public static void main(String[] args) {
        Graph workspace = new Graph();

        // 1. Komponenten erstellen
        ButtonNode switchA = new ButtonNode("Schalter_A");
        ButtonNode switchB = new ButtonNode("Schalter_B");
        OrNode orGate = new OrNode("OR");
        LedNode targetLed = new LedNode("Zentral_LED");

        // 2. Auf dem Grid platzieren (Alle Koordinaten glatt durch 20 teilbar)
        switchA.setPosition(100f, 100f);
        switchB.setPosition(100f, 200f);
        orGate.setPosition(300f, 150f);
        targetLed.setPosition(500f, 150f);

        workspace.addNode(switchA);
        workspace.addNode(switchB);
        workspace.addNode(orGate);
        workspace.addNode(targetLed);

        // =================================================================
        // 3. SAUBERE VERDRAHTUNG ÜBER DAS ODER-GATTER
        // =================================================================

        // Kabel 1: Schalter A -> OR Eingang A (Index 0)
        workspace.addEdge(new Edge(switchA.getOutputs().get(0), orGate.getInputs().get(0)));

        // Kabel 2: Schalter B -> OR Eingang B (Index 1)
        workspace.addEdge(new Edge(switchB.getOutputs().get(0), orGate.getInputs().get(1)));

        // Kabel 3: OR Ausgang -> LED Eingang
        workspace.addEdge(new Edge(orGate.getOutputs().get(0), targetLed.getInputs().get(0)));

        // =================================================================
        // 4. SIMULATION STARTEN
        // =================================================================
        workspace.initializeSimulation();
        System.out.println("[Main-Thread] OR-Verknüpfungs-Schaltung erfolgreich gestartet.");

        EditorWindow editor = new EditorWindow(workspace);

        Thread renderThread = new Thread(() -> {
            Application.launch(editor);
        });
        renderThread.setName("ImGui-Render-Thread");
        renderThread.start();
    }
}
