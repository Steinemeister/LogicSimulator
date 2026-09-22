package logicSimulator.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Edge {
    private final UUID id;
    private final Pin source;
    private final List<Pin> targets;

    public Edge(Pin source) {
        if (source.getType() != Pin.PinType.OUTPUT) {
            throw new IllegalArgumentException("Source must be an OUTPUT pin.");
        }
        this.id = UUID.randomUUID();
        this.source = source;
        this.targets = new ArrayList<>();
    }

    public void addTarget(Pin target) {
        if (target.getType() != Pin.PinType.INPUT) {
            throw new IllegalArgumentException("Target must be an INPUT pin.");
        }
        this.targets.add(target);
    }

    // updates pins
    public List<Pin> update() {
        Pin.PinState sourceState = source.getState();
        List<Pin> pinsToUpdate = new ArrayList<>();

        for (Pin target : targets) {
            if (target.getState() == sourceState) {
                continue;
            }
            pinsToUpdate.add(target);
            target.setState(sourceState);
        }
        return pinsToUpdate;
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
}
