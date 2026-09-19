package logicSimulator.nodes;

import logicSimulator.Graph;
import logicSimulator.Pin;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Node {
    private final String name;
    protected final Map<String, Pin> inputs = new LinkedHashMap<>();
    protected final Map<String, Pin> outputs = new LinkedHashMap<>();

    public Node(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public Map<String, Pin> getInputs() { return inputs; }
    public Map<String, Pin> getOutputs() { return outputs; }

    public abstract void update(Graph graph);
}
