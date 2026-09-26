package logicSimulator.graph;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import logicSimulator.rendering.Renderer;

import java.util.UUID;

public class Pin {
    private final UUID id;
    private final Node parentNode;
    private final PinType type;
    private final int index;
    private PinState state;

    private ImVec2 position;

    public enum PinType { INPUT, OUTPUT }

    public enum PinState { LOW, HIGH }

    public Pin(Node parentNode, PinType type, int index) {
        this.index = index;
        this.id = UUID.randomUUID();
        this.parentNode = parentNode;
        this.type = type;
        this.state = PinState.LOW;
    }

    public void draw(ImDrawList drawList, float offsetX, float offsetY, float zoom) {
        // 1. Berechne die lokale Grid-Position am Rand des Nodes
        // Vertikal verteilen wir die Pins im Abstand von 1 Gitter-Einheit, beginnend ab Reihe 1
        float pinLocalY = (1 + index) * Renderer.GRID_SPACING;

        float pinWorldX = parentNode.getX() * Renderer.GRID_SPACING;
        if (this.type == PinType.OUTPUT) {
            // Outputs sitzen am rechten Rand des Nodes
            pinWorldX += parentNode.getWidth() * Renderer.GRID_SPACING;
        }
        float pinWorldY = (parentNode.getY() * Renderer.GRID_SPACING) + pinLocalY;

        // 2. In Bildschirm-Koordinaten transformieren
        float screenX = (pinWorldX + offsetX) * zoom;
        float screenY = (pinWorldY + offsetY) * zoom;

        // 3. Radius bestimmen (z.B. 5 Pixel Grundradius, skaliert mit Zoom)
        float radius = 5.0f * zoom;

        // 4. Farbe basierend auf dem Logik-Zustand (High = Grün/Rot, Low = Dunkelgrau)
        int color = (state == PinState.HIGH)
                ? ImGui.getColorU32(0.2f, 0.9f, 0.2f, 1.0f)   // Leuchtendes Grün für HIGH
                : ImGui.getColorU32(0.15f, 0.15f, 0.15f, 1.0f); // Dunkles Grau für LOW

        int borderColor = ImGui.getColorU32(0.8f, 0.8f, 0.8f, 1.0f); // Heller Rahmen um den Pin
        float thickness = Math.max(1.0f, 1.5f * zoom);

        // 5. Pin als Kreis auf die DrawList zeichnen
        drawList.addCircleFilled(screenX, screenY, radius, color);
        drawList.addCircle(screenX, screenY, radius, borderColor, 0, thickness);
    }

    public UUID getId() {
        return id;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public PinType getType() {
        return type;
    }

    public PinState getState() {
        return state;
    }

    public void setState(PinState state) {
        this.state = state;
    }

    public ImVec2 getPosition() {
        float pinWorldY = parentNode.getY() + 1 + index;
        float pinWorldX = parentNode.getX();
        if (this.type == PinType.OUTPUT) {
            pinWorldX += parentNode.getWidth();
        }
        return new ImVec2(pinWorldX, pinWorldY);
    }
}
