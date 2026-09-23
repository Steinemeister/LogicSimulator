package logicSimulator.graph.edgeWaypoints;

import imgui.ImVec2;

import java.util.ArrayList;
import java.util.List;

public class WaypointNode {
    private ImVec2 pos;
    private final List<WaypointNode> children;

    public WaypointNode(ImVec2 position) {
        this.pos = position;
        this.children = new ArrayList<>();
    }

    public void addChild(WaypointNode child) {
        this.children.add(child);
    }

    public boolean removeChild(WaypointNode child) {
        return this.children.remove(child);
    }

    public ImVec2 getPos() {
        return pos;
    }

    public void setPos(ImVec2 position) {
        this.pos = position;
    }

    public List<WaypointNode> getChildren() {
        return children;
    }
}
