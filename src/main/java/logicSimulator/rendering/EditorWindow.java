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
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;

public class EditorWindow extends Application {
    private final Graph logicGraph;

    // Zustandsspeicher für das Verschieben und Verkabeln
    private Node draggingNode = null;
    private float mouseOffsetX = 0f;
    private float mouseOffsetY = 0f;
    private Pin sourcePinForNewEdge = null;

    // Globale Rastergröße für das Grid-Snapping
    private final float gridSize = 20f;

    public EditorWindow(Graph logicGraph) {
        this.logicGraph = logicGraph;
    }

    @Override
    protected void configure(Configuration config) {
        config.setTitle("Logic Simulator - Pure Grid Snapping Architecture");
        config.setWidth(1280);
        config.setHeight(720);
    }

    /**
     * Rastet eine Koordinate exakt auf die Rasterlinien ein (für Junctions und Pins).
     * Beispiel bei gridSize 20: 0, 20, 40, 60...
     */
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

        // Farbdefinitionen (ImGui-Format: 0xAABBGGRR)
        int colorNodeBg     = ImGui.getColorU32(0.15f, 0.15f, 0.15f, 1.0f);
        int colorNodeBorder = ImGui.getColorU32(0.40f, 0.40f, 0.40f, 1.0f);
        int colorText       = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);
        int colorPinLow     = ImGui.getColorU32(0.3f, 0.3f, 0.3f, 1.0f);
        int colorPinHigh    = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);
        int colorGridDot    = ImGui.getColorU32(0.25f, 0.25f, 0.25f, 0.5f);
        int colorTempCable  = ImGui.getColorU32(1.0f, 0.6f, 0.0f, 0.8f);

        // =================================================================
        // 1. HINTERGRUND-RASTER (GRID) ZEICHNEN
        // =================================================================
        for (float gx = 0; gx < windowWidth; gx += gridSize) {
            for (float gy = 0; gy < windowHeight; gy += gridSize) {
                drawList.addCircleFilled(gx, gy, 1.0f, colorGridDot);
            }
        }

        // =================================================================
        // 2. MAUS-INTERAKTIONEN (Eindeutige Trennung nach Maus-Tasten)
        // =================================================================
        float mouseX = ImGui.getMousePosX();
        float mouseY = ImGui.getMousePosY();

        // --- LINKE MAUSTASTE: Aktionen & Kabel ---
        if (ImGui.getIO().getKeyShift() && ImGui.isMouseClicked(0)) {
            // Shift + Linksklick -> Kabel splitten
            Edge clickedEdge = logicGraph.getEdgeAt(mouseX, mouseY, 6f);
            if (clickedEdge != null) {
                String junctionName = "Junc_" + System.currentTimeMillis();
                logicGraph.splitEdgeWithJunction(clickedEdge, junctionName, mouseX, mouseY, gridSize);
            }
        } else if (ImGui.isMouseClicked(0)) {
            // Reiner Linksklick -> Kabel ziehen oder Button umschalten
            Pin clickedPin = logicGraph.getAnyPinAt(mouseX, mouseY, 6f);

            if (clickedPin != null) {
                if (sourcePinForNewEdge == null) {
                    sourcePinForNewEdge = clickedPin; // Kabel ziehen starten
                } else {
                    if (sourcePinForNewEdge != clickedPin) {
                        logicGraph.addEdge(new Edge(sourcePinForNewEdge, clickedPin));
                        logicGraph.initializeSimulation();
                    }
                    sourcePinForNewEdge = null; // Kabelziehen beenden
                }
            } else {
                Node hitNode = logicGraph.getNodeAt(mouseX, mouseY);
                if (hitNode instanceof ButtonNode) {
                    ((ButtonNode) hitNode).toggle(logicGraph);
                }
            }
        }

        // Rechtsklick bricht das Kabelziehen ab
        if (ImGui.isMouseClicked(1)) {
            sourcePinForNewEdge = null;
        }

        // --- RECHTE MAUSTASTE: Greifen & Bewegen ---
        if (ImGui.isMouseClicked(1)) {
            Node hitNode = logicGraph.getNodeAt(mouseX, mouseY);
            if (hitNode != null) {
                draggingNode = hitNode;
                mouseOffsetX = mouseX - draggingNode.getX();
                mouseOffsetY = mouseY - draggingNode.getY();
            }
        }

        if (draggingNode != null && ImGui.isMouseDragging(1)) {
            float rawTargetX = mouseX - mouseOffsetX;
            float rawTargetY = mouseY - mouseOffsetY;

            draggingNode.setPosition(snap(rawTargetX), snap(rawTargetY));
        }

        if (ImGui.isMouseReleased(1)) {
            draggingNode = null;
        }

        // =================================================================
        // 3. SEITE RENDERN (Kabel, Vorschau, Gatter, Pins)
        // =================================================================

        // A) Bestehende Kabel rechtwinklig zeichnen
        for (Edge edge : logicGraph.getEdges()) {
            Pin src = logicGraph.findPinGlobally(edge.getSourcePinId());
            Pin dest = logicGraph.findPinGlobally(edge.getDestPinId());

            if (src != null && dest != null) {
                int cableColor = (src.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                float x1 = src.getAbsoluteX();
                float y1 = src.getAbsoluteY();
                float x2 = dest.getAbsoluteX();
                float y2 = dest.getAbsoluteY();

                if (x1 == x2 || y1 == y2) {
                    drawList.addLine(x1, y1, x2, y2, cableColor, 2.0f);
                } else {
                    float midX = x1 + (x2 - x1) / 2f;
                    drawList.addLine(x1, y1, midX, y1, cableColor, 2.0f);
                    drawList.addLine(midX, y1, midX, y2, cableColor, 2.0f);
                    drawList.addLine(midX, y2, x2, y2, cableColor, 2.0f);
                }
            }
        }

        // B) Temporäres Kabel zeichnen (wieder im S-Knick)
        if (sourcePinForNewEdge != null) {
            float x1 = sourcePinForNewEdge.getAbsoluteX();
            float y1 = sourcePinForNewEdge.getAbsoluteY();
            float x2 = snap(mouseX);
            float y2 = snap(mouseY);

            float midX = x1 + (x2 - x1) / 2f;
            drawList.addLine(x1, y1, midX, y1, colorTempCable, 1.5f);
            drawList.addLine(midX, y1, midX, y2, colorTempCable, 1.5f);
            drawList.addLine(midX, y2, x2, y2, colorTempCable, 1.5f);
        }

        // C) Gatter, Junctions und Pins ohne jegliche visuelle Verzerrung rendern
        for (Node node : logicGraph.getNodes()) {
            if (node instanceof JunctionNode) {
                Pin p = node.getInputs().get(0);
                int junctionColor = (p.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(p.getAbsoluteX(), p.getAbsoluteY(), 5f, junctionColor);
                continue;
            }

            // Ganz sauberes, direktes Abgreifen der Datenstruktur
            float x = node.getX();
            float y = node.getY();
            float w = node.getWidth();
            float h = node.getHeight();

            int currentBgColor = colorNodeBg;
            if (node instanceof LedNode) {
                currentBgColor = ((LedNode) node).isOn() ? ImGui.getColorU32(0.0f, 0.6f, 0.0f, 1.0f) : ImGui.getColorU32(0.2f, 0.0f, 0.0f, 1.0f);
            } else if (node instanceof ButtonNode && ((ButtonNode) node).isPressed()) {
                currentBgColor = ImGui.getColorU32(0.25f, 0.35f, 0.25f, 1.0f);
            }

            // Keine Offsets mehr – das mathematische Snapping hat y bereits perfekt ausgerichtet!
            drawList.addRectFilled(x, y, x + w, y + h, currentBgColor, 4.0f);
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
