package logicSimulator.graph;

import imgui.ImVec2;
import logicSimulator.graph.edgeWaypoints.WaypointNode;

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
