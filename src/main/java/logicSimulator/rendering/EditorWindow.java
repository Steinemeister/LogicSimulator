package logicSimulator.rendering;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.JunctionNode;
import logicSimulator.graph.nodes.Node;
import logicSimulator.graph.nodes.Pin;

public class EditorWindow extends Application {
    private final Graph logicGraph;

    private Node draggingNode = null;

    public EditorWindow(Graph logicGraph) {
        this.logicGraph = logicGraph;
    }

    /**
     * Konfiguriert die Fenster-Eigenschaften (Titel, Größe) vor dem Start.
     */
    @Override
    protected void configure(Configuration config) {
        config.setTitle("Modular Logic Simulator");
        config.setWidth(1280);
        config.setHeight(720);
    }

    /**
     * Die Kern-Render-Schleife von ImGui.
     * Alles, was hier drin steht, wird jeden Frame neu gezeichnet (Immediate Mode).
     */
    @Override
    public void process() {
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());

        int windowFlags = imgui.flag.ImGuiWindowFlags.NoTitleBar
                | imgui.flag.ImGuiWindowFlags.NoResize
                | imgui.flag.ImGuiWindowFlags.NoMove
                | imgui.flag.ImGuiWindowFlags.NoCollapse
                | imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus;

        ImGui.begin("CanvasWindow", windowFlags);

        // =================================================================
// MAUS-INTERAKTIONEN (Verschieben & Splitten)
// =================================================================
        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

// FALL 1: SHIFT-KLICK -> Kabel splitten
        if (ImGui.getIO().getKeyShift() && ImGui.isMouseClicked(0)) {
            // Toleranz von 6 Pixeln, um das Kabel mit der Maus gut zu treffen
            Edge clickedEdge = logicGraph.getEdgeAt(mouseX, mouseY, 6f);

            if (clickedEdge != null) {
                // Erzeuge einen eindeutigen Namen anhand des aktuellen Zeitstempels
                String junctionName = "Junc_" + System.currentTimeMillis();

                // Nutze deine unfehlbare Logik-Methode zum Aufbrechen!
                JunctionNode newJunction = logicGraph.splitEdgeWithJunction(clickedEdge, junctionName);

                // Platziere den neuen Knotenpunkt EXAKT unter der Maus
                newJunction.setPosition(mouseX, mouseY);
            }
        }
// FALL 2: NORMALER KLICK -> Gatter greifen (Nur wenn Shift NICHT gedrückt ist)
        else if (ImGui.isMouseClicked(0)) {
            Node hitNode = logicGraph.getNodeAt(mouseX, mouseY);
            if (hitNode != null && !(hitNode instanceof JunctionNode)) {
                draggingNode = hitNode;
            }
        }

// Festhalten und bewegen
        if (draggingNode != null && ImGui.isMouseDragging(0)) {
            float deltaX = ImGui.getIO().getMouseDeltaX();
            float deltaY = ImGui.getIO().getMouseDeltaY();
            draggingNode.setPosition(draggingNode.getX() + deltaX, draggingNode.getY() + deltaY);
        }

// Loslassen
        if (ImGui.isMouseReleased(0)) {
            draggingNode = null;
        }

        // =================================================================
        // ZEICHEN-LOGIK (Unverändert)
        // =================================================================
        ImDrawList drawList = ImGui.getWindowDrawList();
        int colorNodeBg     = ImGui.getColorU32(0.15f, 0.15f, 0.15f, 1.0f);
        int colorNodeBorder = ImGui.getColorU32(0.40f, 0.40f, 0.40f, 1.0f);
        int colorText       = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);
        int colorPinLow     = ImGui.getColorU32(0.3f, 0.3f, 0.3f, 1.0f);
        int colorPinHigh    = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);

        // Kabel zeichnen
        for (Edge edge : logicGraph.getEdges()) {
            Pin src = logicGraph.findPinGlobally(edge.getSourcePinId());
            Pin dest = logicGraph.findPinGlobally(edge.getDestPinId());
            if (src != null && dest != null) {
                int cableColor = (src.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addLine(src.getAbsoluteX(), src.getAbsoluteY(), dest.getAbsoluteX(), dest.getAbsoluteY(), cableColor, 2.0f);
            }
        }

        // Gatter und Pins zeichnen
        for (Node node : logicGraph.getNodes()) {
            if (node instanceof JunctionNode) {
                Pin p = node.getInputs().get(0);
                int junctionColor = (p.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(p.getAbsoluteX(), p.getAbsoluteY(), 5f, junctionColor);
                continue;
            }

            float x = node.getX();
            float y = node.getY();
            float w = node.getWidth();
            float h = node.getHeight();

            drawList.addRectFilled(x, y, x + w, y + h, colorNodeBg, 4.0f);
            drawList.addRect(x, y, x + w, y + h, colorNodeBorder, 4.0f, 0, 1.5f);
            drawList.addText(x + 10f, y + (h / 2f) - 6f, colorText, node.getName());

            for (Pin pin : node.getInputs()) {
                int pinColor = (pin.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, pinColor);
                drawList.addCircle(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, colorNodeBorder, 0, 1f);
            }

            for (Pin pin : node.getOutputs()) {
                int pinColor = (pin.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, pinColor);
                drawList.addCircle(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, colorNodeBorder, 0, 1f);
            }
        }

        ImGui.end();
    }
}
