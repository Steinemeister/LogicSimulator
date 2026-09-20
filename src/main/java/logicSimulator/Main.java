package logicSimulator;

import imgui.app.Application;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.*;
import logicSimulator.rendering.EditorWindow;

public class Main {
    public static void main(String[] args) {
        // 1. Initialisiere die logische Simulations-Arbeitsfläche
        Graph workspace = new Graph();

        // 2. Erstelle die logischen und visuellen Knoten
        ButtonNode inputButtonA = new ButtonNode("A");
        ButtonNode inputButtonB = new ButtonNode("B"); // Dient als zweiter Input fürs AND
        AndNode andGate = new AndNode("AND");
        NotNode notGate = new NotNode("NOT");
        LedNode outputLed = new LedNode("LED");

        // 3. Platziere die Knoten räumlich im Editor-Koordinatensystem
        inputButtonA.setPosition(100f, 100f);
        inputButtonB.setPosition(100f, 200f);
        andGate.setPosition(300f, 140f);
        notGate.setPosition(500f, 140f);
        outputLed.setPosition(700f, 140f);

        // 4. Registriere alle Knoten im Hauptgraphen
        workspace.addNode(inputButtonA);
        workspace.addNode(inputButtonB);
        workspace.addNode(andGate);
        workspace.addNode(notGate);
        workspace.addNode(outputLed);

        // =================================================================
        // 5. VERDRAHTUNG DER GEOMETRISCHEN PINS (Absolut sicher via UUIDs)
        // =================================================================

        // Kabel 1: Schalter A -> AND Eingang 1 (Index 0)
        workspace.addEdge(new Edge(inputButtonA.getOutputs().get(0), andGate.getInputs().get(0)));

        // Kabel 2: Schalter B -> AND Eingang 2 (Index 1)
        workspace.addEdge(new Edge(inputButtonB.getOutputs().get(0), andGate.getInputs().get(1)));

        // Kabel 3: AND Ausgang -> NOT Eingang
        workspace.addEdge(new Edge(andGate.getOutputs().get(0), notGate.getInputs().get(0)));

        // Kabel 4: NOT Ausgang -> LED Eingang
        workspace.addEdge(new Edge(notGate.getOutputs().get(0), outputLed.getInputs().get(0)));

        // =================================================================
        // 6. INITIALISIERUNG & THREAD-START
        // =================================================================

        // Schalter B standardmäßig direkt einschalten, damit wir das AND-Gatter
        // später mit nur einem Klick auf Schalter A testen können (1 AND 1 = 1)
        inputButtonB.toggle(workspace);

        // Simulation einmalig aufwecken, um Startzustände zu berechnen
        workspace.initializeSimulation();
        System.out.println("[Main-Thread] Simulations-Engine erfolgreich initialisiert.");

        // Erstelle die ImGui-Anwendung
        EditorWindow editor = new EditorWindow(workspace);

        // Starte das interaktive Rendering auf dem separaten Grafik-Thread
        Thread renderThread = new Thread(() -> {
            System.out.println("[Render-Thread] Starte GLFW, OpenGL & ImGui...");
            Application.launch(editor);
            System.out.println("[Render-Thread] Fenster wurde vom Benutzer geschlossen.");
        });

        renderThread.setName("ImGui-Render-Thread");
        renderThread.start();
    }
}
