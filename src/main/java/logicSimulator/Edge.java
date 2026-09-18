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

    public void transmitSignal() {
        Pin sourcePin = sourceNode.getOutputs().get(sourcePinName);
        Pin destPin = destNode.getInputs().get(destPinName);

        if (sourcePin != null && destPin != null) {
            destPin.setState(sourcePin.getState());
        }
    }

    public Node getSourceNode() { return sourceNode; }
    public Node getDestNode() { return destNode; }
}
