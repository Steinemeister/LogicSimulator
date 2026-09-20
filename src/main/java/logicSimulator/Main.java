package logicSimulator;

import imgui.app.Application;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.NodeRegistry;
import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.*;
import logicSimulator.rendering.EditorWindow;

public class Main {
    public static void main(String[] args) {
        Graph workspace = new Graph();

        // 1. Das zentrale Register erstellen
        NodeRegistry registry = new NodeRegistry();

        // 2. Die Standard-Baupläne registrieren (Verwendung von schlichten Lambdas)
        registry.registerType("AND Gatter", AndNode::new);
        registry.registerType("NAND Gatter", NandNode::new);
        registry.registerType("OR Gatter", OrNode::new);
        registry.registerType("NOT Inverter", NotNode::new);
        registry.registerType("Schalter", ButtonNode::new);
        registry.registerType("LED Lampe", LedNode::new);
        registry.registerType("XOR Gatter", XorNode::new);
        registry.registerType("XNOR Gatter", XnorNode::new);

        // Wenn der Benutzer später im UI ein Custom-Modul speichert, ruft dein Code einfach auf:
        // registry.registerType("MeinSuperModul", () -> new CustomModuleNode("MeinSuperModul"));
        // und es taucht augenblicklich in der Sidebar auf!

        workspace.initializeSimulation();

        // 3. Register an das Fenster übergeben
        EditorWindow editor = new EditorWindow(workspace, registry);

        Thread renderThread = new Thread(() -> {
            Application.launch(editor);
        });
        renderThread.setName("ImGui-Render-Thread");
        renderThread.start();
    }
}
