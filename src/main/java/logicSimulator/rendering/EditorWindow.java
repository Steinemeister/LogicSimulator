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
        // Fenster nimmt den gesamten Bildschirm ein
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());

        int windowFlags = imgui.flag.ImGuiWindowFlags.NoTitleBar
                | imgui.flag.ImGuiWindowFlags.NoResize
                | imgui.flag.ImGuiWindowFlags.NoMove
                | imgui.flag.ImGuiWindowFlags.NoCollapse
                | imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus;

        ImGui.begin("CanvasWindow", windowFlags);

        // Wir holen uns die Zeichen-Leinwand für das aktuelle Fenster
        ImDrawList drawList = ImGui.getWindowDrawList();

        // Farbdefinitionen (ImGui nutzt das Format 0xAABBGGRR)
        int colorNodeBg     = ImGui.getColorU32(0.15f, 0.15f, 0.15f, 1.0f); // Dunkelgrau
        int colorNodeBorder = ImGui.getColorU32(0.40f, 0.40f, 0.40f, 1.0f); // Hellgrau
        int colorText       = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);    // Weiß
        int colorPinLow     = ImGui.getColorU32(0.3f, 0.3f, 0.3f, 1.0f);    // Mattes Grau
        int colorPinHigh    = ImGui.getColorU32(0.0f, 1.0f, 0.0f, 1.0f);    // Leuchtend Grün

        // =================================================================
        // 1. KABEL (EDGES) ZEICHNEN (Zuerst, damit sie hinter den Gattern liegen)
        // =================================================================
        for (Edge edge : logicGraph.getEdges()) {
            Pin src = logicGraph.findPinGlobally(edge.getSourcePinId());
            Pin dest = logicGraph.findPinGlobally(edge.getDestPinId());

            if (src != null && dest != null) {
                // Bestimme die Farbe basierend auf dem logischen Zustand
                int cableColor = (src.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;

                // Zeichne eine einfache Linie (Später können wir hier Bezier-Kurven nutzen)
                drawList.addLine(
                        src.getAbsoluteX(), src.getAbsoluteY(),
                        dest.getAbsoluteX(), dest.getAbsoluteY(),
                        cableColor,
                        2.0f // Dicke der Linie in Pixeln
                );
            }
        }

        // =================================================================
        // 2. KNOTEN (NODES) UND PINS ZEICHNEN
        // =================================================================
        for (Node node : logicGraph.getNodes()) {
            // Sonderfall: Junctions zeichnen wir nicht als Kasten, sondern nur als Punkt
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

            // Gehäuse (Hintergrund-Rechteck) mit abgerundeten Ecken (4.0f) zeichnen
            drawList.addRectFilled(x, y, x + w, y + h, colorNodeBg, 4.0f);
            drawList.addRect(x, y, x + w, y + h, colorNodeBorder, 4.0f, 0, 1.5f);

            // Gatter-Name zentriert auf das Gehäuse schreiben
            // Wir versetzen den Text leicht nach innen (z.B. X+10, Y+ Höhe/2 - Text_Offset)
            drawList.addText(x + 10f, y + (h / 2f) - 6f, colorText, node.getName());

            // --- INPUT PINS ZEICHNEN ---
            for (Pin pin : node.getInputs()) {
                int pinColor = (pin.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                // Zeichne gefüllten Kreis an der absoluten Position des Pins (Radius 4f)
                drawList.addCircleFilled(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, pinColor);
                // Kleiner Rahmen um den Pin für bessere Sichtbarkeit
                drawList.addCircle(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, colorNodeBorder, 0, 1f);
            }

            // --- OUTPUT PINS ZEICHNEN ---
            for (Pin pin : node.getOutputs()) {
                int pinColor = (pin.getState() == Pin.State.HIGH) ? colorPinHigh : colorPinLow;
                drawList.addCircleFilled(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, pinColor);
                drawList.addCircle(pin.getAbsoluteX(), pin.getAbsoluteY(), 4f, colorNodeBorder, 0, 1f);
            }
        }

        ImGui.end();
    }
}
