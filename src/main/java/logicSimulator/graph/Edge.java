package logicSimulator.graph;

import logicSimulator.graph.nodes.Pin;
import logicSimulator.graph.nodes.Node;

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

    public boolean isPointNearLine(float px, float py, float tolerance) {
        float x1 = sourceNode.getOutputs().get(sourcePinName).getAbsoluteX();
        float y1 = sourceNode.getOutputs().get(sourcePinName).getAbsoluteY();
        float x2 = destNode.getInputs().get(destPinName).getAbsoluteX();
        float y2 = destNode.getInputs().get(destPinName).getAbsoluteY();

        float l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (l2 == 0) return false;

        float t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));

        float projX = x1 + t * (x2 - x1);
        float projY = y1 + t * (y2 - y1);
        float distance = (float) Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));

        return distance <= tolerance;
    }

    public Node getSourceNode() { return sourceNode; }
    public String getSourcePinName() { return sourcePinName; }
    public Node getDestNode() { return destNode; }
    public String getDestPinName() { return destPinName; }
}
