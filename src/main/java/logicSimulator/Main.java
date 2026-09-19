package logicSimulator;

import logicSimulator.graph.Edge;
import logicSimulator.graph.Graph;
import logicSimulator.graph.nodes.*;
import logicSimulator.graph.nodes.io.ButtonNode;
import logicSimulator.graph.nodes.io.LedNode;
import logicSimulator.graph.nodes.logic.*;
import logicSimulator.graph.nodes.module.CustomModuleNode;

public class Main {
    public static void main(String[] args) {
        CustomModuleNode meinModul = new CustomModuleNode("MeinInverter");
        meinModul.addExternalInput("Eingang_1");   // Landet bei inputs.get(0)
        meinModul.addExternalOutput("Ausgang_1"); // Landet bei outputs.get(0)

        Graph innerGraph = meinModul.getInternalGraph();
        NotNode internesNot = new NotNode("NOT");
        innerGraph.addNode(internesNot);

// Verdrahtung im Inneren absolut fingersicher per UUID verknüpfen:
// Kabel 1: Vom inneren Input-Knoten zum NOT-Eingang
        Pin vonInput = meinModul.getInternalInputNodes().get(0).getOutputs().get(0);
        Pin zuNot = internesNot.getInputs().get(0);
        innerGraph.addEdge(new Edge(vonInput, zuNot));

// Kabel 2: Vom NOT-Ausgang zum inneren Output-Knoten
        Pin vonNot = internesNot.getOutputs().get(0);
        Pin zuOutput = meinModul.getInternalOutputNodes().get(0).getInputs().get(0);
        innerGraph.addEdge(new Edge(vonNot, zuOutput));
    }
}
