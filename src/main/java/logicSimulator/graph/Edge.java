package logicSimulator.graph;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import logicSimulator.graph.edgeWaypoints.WaypointNode;
import logicSimulator.rendering.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Edge {
    private final UUID id;
    private final Pin source;
    private final List<Pin> targets;

    private final WaypointNode rootWaypoint;

    public Edge(Pin source) {
        if (source.getType() != Pin.PinType.OUTPUT) {
            throw new IllegalArgumentException("Source must be an OUTPUT pin.");
        }
        this.id = UUID.randomUUID();
        this.source = source;
        this.targets = new ArrayList<>();

        this.rootWaypoint = new WaypointNode(source.getPosition());
    }

    public void addTarget(Pin target) {
        if (target.getType() != Pin.PinType.INPUT) {
            throw new IllegalArgumentException("Target must be an INPUT pin.");
        }
        this.targets.add(target);

        WaypointNode targetWaypoint = new WaypointNode(target.getPosition());
        this.rootWaypoint.addChild(targetWaypoint);
    }

    public void render(ImDrawList drawList, float offsetX, float offsetY, float zoom, boolean selected) {
        int color = (getState() == Pin.PinState.HIGH)
                ? ImGui.getColorU32(0.2f, 0.9f, 0.2f, 1.0f)
                : ImGui.getColorU32(0.6f, 0.6f, 0.6f, 1.0f);

        if (selected) {
            color = ImGui.getColorU32(1.0f, 0.8f, 0.0f, 1.0f);
        }

        float baseThickness = selected ? 4.0f : 2.5f;
        float thickness = baseThickness * zoom;

        // KORREKTUR: Synchronisiere die Positionen aller Wegpunkte mit den echten Pin-Positionen
        updateRootPosition();
        for (int i = 0; i < targets.size(); i++) {
            // Da die Kinder in derselben Reihenfolge wie die Targets hinzugefügt wurden:
            if (i < rootWaypoint.getChildren().size()) {
                rootWaypoint.getChildren().get(i).setPos(targets.get(i).getPosition());
            }
        }

        // Rekursiven Render-Prozess starten
        renderWaypointTree(drawList, rootWaypoint, color, thickness, offsetX, offsetY, zoom);
    }

    private void renderWaypointTree(ImDrawList drawList, WaypointNode current, int color, float thickness, float offsetX, float offsetY, float zoom) {
        float p1x = (current.getPos().x * Renderer.GRID_SPACING + offsetX) * zoom;
        float p1y = (current.getPos().y * Renderer.GRID_SPACING + offsetY) * zoom;

        for (WaypointNode child : current.getChildren()) {
            float p2x = (child.getPos().x * Renderer.GRID_SPACING + offsetX) * zoom;
            float p2y = (child.getPos().y * Renderer.GRID_SPACING + offsetY) * zoom;

            float controlOffset = (Renderer.GRID_SPACING * 0.5f) * zoom;

            float cp1x = p1x + controlOffset;
            float cp1y = p1y;
            float cp2x = p2x - controlOffset;
            float cp2y = p2y;

            drawList.addBezierCubic(p1x, p1y, cp1x, cp1y, cp2x, cp2y, p2x, p2y, color, thickness);

            renderWaypointTree(drawList, child, color, thickness, offsetX, offsetY, zoom);
        }
    }

    public Pin.PinState getState() {
        return source.getState();
    }

    public UUID getId() {
        return id;
    }

    public Pin getSource() {
        return source;
    }

    public List<Pin> getTargets() {
        return targets;
    }

    public void updateRootPosition() {
        this.rootWaypoint.setPos(source.getPosition());
    }
    public WaypointNode getRootWaypoint() {
        return rootWaypoint;
    }
}
