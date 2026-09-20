package logicSimulator;

import imgui.app.Application;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.logic.*;
import logicSimulator.rendering.EditorWindow;

public class Main {
    public static void main(String[] args) {
        // 1. Initialisiere die Logik-Engine auf dem Haupt-Thread
        Graph mainGraph = new Graph();

        AndNode and1 = new AndNode("AND_1");
        and1.setPosition(100f, 150f);

        NotNode not1 = new NotNode("NOT_1");
        not1.setPosition(300f, 150f);

        mainGraph.addNode(and1);
        mainGraph.addNode(not1);

// Kabel ziehen: AND-Out zu NOT-In
        mainGraph.addEdge(new Edge(and1.getOutputs().get(0), not1.getInputs().get(0)));
        mainGraph.initializeSimulation();

        System.out.println("[Main-Thread] Simulations-Engine gestartet.");

        // 2. Erstelle das ImGui-Fenster-Objekt
        EditorWindow editor = new EditorWindow(mainGraph);

        // 3. Starte das Rendering auf einem SEPARATEN RENDER-THREAD
        Thread renderThread = new Thread(() -> {
            System.out.println("[Render-Thread] Starte GLFW & ImGui-Kontext...");
            // launch() blockiert den Thread so lange, wie das Fenster geöffnet ist
            Application.launch(editor);
            System.out.println("[Render-Thread] Fenster geschlossen.");
        });

        // Name zuweisen für leichteres Debugging
        renderThread.setName("ImGui-Render-Thread");
        renderThread.start();
    }
}
