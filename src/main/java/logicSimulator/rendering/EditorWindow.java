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

    // NEU: Wir merken uns den genauen Offset zwischen dem Gatter-Ursprung (X/Y)
    // und der genauen Position, an der die Maus das Gatter angeklickt hat.
    private float mouseOffsetX = 0f;
    private float mouseOffsetY = 0f;

    private final float gridSize = 20f;

    public EditorWindow(Graph logicGraph) {
        this.logicGraph = logicGraph;
    }

    @Override
    protected void configure(Configuration config) {
        config.setTitle("Modular Logic Simulator with Fixed Grid");
        config.setWidth(1280);
        config.setHeight(720);
    }

    private float snap(float value) {
        return Math.round(value / gridSize) * gridSize;
    }

    @Override
    public void process() {
        float windowWidth = ImGui.getIO().getDisplaySizeX();
        float windowHeight = ImGui.getIO().getDisplaySizeY();

        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(windowWidth, windowHeight);

        int windowFlags = imgui.flag.ImGuiWindowFlags.NoTitleBar
                | imgui.flag.ImGuiWindowFlags.NoResize
                | imgui.flag.ImGuiWindowFlags.NoMove
                | imgui.flag.ImGuiWindowFlags.NoCollapse
                | imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus;

        ImGui.begin("CanvasWindow", windowFlags);
        ImDrawList drawList = ImGui.getWindowDrawList();

        // Farben & Raster zeichnen (Unverändert)
        int colorNodeBg     = ImGui.getColorU32(0.15f, 0.15f, 0.15f, 1.0f);
        int colorNodeBorder = ImGui.getColorU32(0.40f, 0.40f, 0.40f, 1.0f);
        int colorText       = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);
        int colorPinLow     = ImGui.getColorU32(0.3f, 0.3f, 0.3f, 1.0f);
        int colorPinHigh    = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);
        int colorGridDot    = ImGui.getColorU32(0.25f, 0.25f, 0.25f, 0.5f);

        for (float gx = 0; gx < windowWidth; gx += gridSize) {
            for (float gy = 0; gy < windowHeight; gy += gridSize) {
                drawList.addCircleFilled(gx, gy, 1.0f, colorGridDot);
            }
        }

        // =================================================================
        // KORRIGIERTE MAUS-INTERAKTIONEN (OFFSET-BASIERT)
        // =================================================================
        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

        // 1. Kabel splitten via Shift-Klick
        if (ImGui.getIO().getKeyShift() && ImGui.isMouseClicked(0)) {
            Edge clickedEdge = logicGraph.getEdgeAt(mouseX, mouseY, 6f);
            if (clickedEdge != null) {
                String junctionName = "Junc_" + System.currentTimeMillis();
                JunctionNode newJunction = logicGraph.splitEdgeWithJunction(clickedEdge, junctionName);
                newJunction.setPosition(snap(mouseX), snap(mouseY));
            }
        }
        // 2. Normaler Klick -> Greift Gatter ODER Junctions und berechnet den Offset
        else if (ImGui.isMouseClicked(0)) {
            Node hitNode = logicGraph.getNodeAt(mouseX, mouseY);
            if (hitNode != null) {
                draggingNode = hitNode;
                // Berechne, wie weit die Maus vom linken oberen Rand des Knotens entfernt ist
                mouseOffsetX = mouseX - draggingNode.getX();
                mouseOffsetY = mouseY - draggingNode.getY();
            }
        }

        // 3. Festhalten und bewegen mit absolut flüssigem Snapping
        if (draggingNode != null && ImGui.isMouseDragging(0)) {
            // Berechne die ungefilterte Wunschposition des Gatters basierend auf der aktuellen Mausposition
            float rawTargetX = mouseX - mouseOffsetX;
            float rawTargetY = mouseY - mouseOffsetY;

            // Erst JETZT wird die Gesamtposition auf das Raster gerundet.
            // Da mouseX sich kontinuierlich bewegt, springt das Gatter jetzt
            // absolut butterweich von Punkt zu Punkt, egal wie langsam du ziehst!
            draggingNode.setPosition(snap(rawTargetX), snap(rawTargetY));
        }

        // 4. Loslassen
        if (ImGui.isMouseReleased(0)) {
            draggingNode = null;
        }

        // =================================================================
        // ZEICHEN-LOGIK (Unverändert)
        // =================================================================
        for (Edge edge : logicGraph.getEdges()) {
            Pin src = logicGraph.findPinGlobally(edge.getSourcePinId());
            Pin dest = logicGraph.findPinGlobally(edge.getDestPinId());
            if (src != null && dest != null) {
                int cableColor = (src.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addLine(src.getAbsoluteX(), src.getAbsoluteY(), dest.getAbsoluteX(), dest.getAbsoluteY(), cableColor, 2.0f);
            }
        }

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
