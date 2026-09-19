package logicSimulator;

import logicSimulator.nodes.Node;

public class Edge {
    private final Node sourceNode;
    private final String sourcePinName;
    private final Node destNode;
    private final String destPinName;

    public Edge(Node sourceNode, String sourcePinName, Node destNode, String destPinName) {
        this.sourceNode = sourceNode;
        this.sourcePinName = sourcePinName;
        this.destNode = destNode;
        this.destPinName = destPinName;
    }

    public void transmitSignal(Graph graph) {
        Pin sourcePin = sourceNode.getOutputs().get(sourcePinName);
        Pin destPin = destNode.getInputs().get(destPinName);

        if (sourcePin != null && destPin != null) {
            // Ein Kabel leitet das Signal sofort weiter (Delay = 0)
            graph.queueEvent(destPin, sourcePin.getState(), 0);
        }
    }

    public Node getSourceNode() { return sourceNode; }
    public String getSourcePinName() { return sourcePinName; }
    public Node getDestNode() { return destNode; }
    public String getDestPinName() { return destPinName; }
}
