package logicSimulator.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class Node {
    private final UUID id;
    private final String nodeTypeName;
    private final List<Pin> inputPins;
    private final List<Pin> outputPins;

    public Node(String nodeTypeName) {
        this.id = UUID.randomUUID();
        this.nodeTypeName = nodeTypeName;
        this.inputPins = new ArrayList<>();
        this.outputPins = new ArrayList<>();
    }

    /*
    updates output pins based on input pins states
    returns list of pins that changed state
     */
    public abstract void update(Graph graph);

    public UUID getId() {
        return id;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public List<Pin> getInputPins() {
        return inputPins;
    }

    public List<Pin> getOutputPins() {
        return outputPins;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
